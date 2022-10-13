package dev.xdark.ssvm;

import dev.xdark.ssvm.api.VMInterface;
import dev.xdark.ssvm.classloading.BootClassFinder;
import dev.xdark.ssvm.classloading.ClassDefiner;
import dev.xdark.ssvm.classloading.ClassLoaders;
import dev.xdark.ssvm.classloading.ParsedClassData;
import dev.xdark.ssvm.execution.ExecutionEngine;
import dev.xdark.ssvm.execution.Locals;
import dev.xdark.ssvm.fs.FileDescriptorManager;
import dev.xdark.ssvm.jvm.ManagementInterface;
import dev.xdark.ssvm.memory.allocation.MemoryAllocator;
import dev.xdark.ssvm.memory.management.MemoryManager;
import dev.xdark.ssvm.memory.management.StringPool;
import dev.xdark.ssvm.mirror.MirrorFactory;
import dev.xdark.ssvm.mirror.member.JavaMethod;
import dev.xdark.ssvm.mirror.type.InstanceClass;
import dev.xdark.ssvm.mirror.type.JavaClass;
import dev.xdark.ssvm.natives.IntrinsicsNatives;
import dev.xdark.ssvm.nt.NativeLibraryManager;
import dev.xdark.ssvm.operation.VMOperations;
import dev.xdark.ssvm.symbol.InitializedPrimitives;
import dev.xdark.ssvm.symbol.InitializedSymbols;
import dev.xdark.ssvm.symbol.Primitives;
import dev.xdark.ssvm.symbol.Symbols;
import dev.xdark.ssvm.synchronizer.ObjectSynchronizer;
import dev.xdark.ssvm.thread.JavaThread;
import dev.xdark.ssvm.thread.OSThread;
import dev.xdark.ssvm.thread.ThreadManager;
import dev.xdark.ssvm.thread.ThreadStorage;
import dev.xdark.ssvm.tz.TimeManager;
import dev.xdark.ssvm.util.Reflection;
import dev.xdark.ssvm.value.InstanceValue;

import java.nio.ByteOrder;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class VirtualMachine implements VirtualMachineFacade {

	private final AtomicReference<InitializationState> state = new AtomicReference<>(InitializationState.UNINITIALIZED);
	private final MemoryAllocator memoryAllocator;
	private final MemoryManager memoryManager;
	private final ClassDefiner classDefiner;
	private final ThreadManager threadManager;
	private final FileDescriptorManager fileDescriptorManager;
	private final NativeLibraryManager nativeLibraryManager;
	private final StringPool stringPool;
	private final ManagementInterface managementInterface;
	private final TimeManager timeManager;
	private final ClassLoaders classLoaders;
	private final ExecutionEngine executionEngine;
	private final LinkResolver linkResolver;
	private final MirrorFactory mirrorFactory;
	private final ObjectSynchronizer objectSynchronizer;
	private final BootClassFinder bootClassFinder;
	private final Map<String, String> properties;
	private final Map<String, String> env;
	private final VMInterface vmInterface;
	private final Reflection reflection;
	private final VMOperations operations;
	private Symbols symbols;
	private Primitives primitives;
	private volatile InstanceValue systemThreadGroup;
	private volatile InstanceValue mainThreadGroup;

	public VirtualMachine(
		VMInterface vmInterface,
		MemoryAllocator memoryAllocator,
		MemoryManager memoryManager,
		ClassDefiner classDefiner,
		ThreadManager threadManager,
		FileDescriptorManager fileDescriptorManager,
		NativeLibraryManager nativeLibraryManager,
		StringPool stringPool,
		ManagementInterface managementInterface,
		TimeManager timeManager,
		ClassLoaders classLoaders,
		ExecutionEngine executionEngine,
		LinkResolver linkResolver,
		MirrorFactory mirrorFactory,
		ObjectSynchronizer objectSynchronizer,
		BootClassFinder bootClassFinder,
		Map<String, String> properties,
		Map<String, String> env
	) {
		this.vmInterface = vmInterface;
		this.memoryAllocator = memoryAllocator;
		this.memoryManager = memoryManager;
		this.classDefiner = classDefiner;
		this.threadManager = threadManager;
		this.fileDescriptorManager = fileDescriptorManager;
		this.nativeLibraryManager = nativeLibraryManager;
		this.stringPool = stringPool;
		this.managementInterface = managementInterface;
		this.timeManager = timeManager;
		this.classLoaders = classLoaders;
		this.executionEngine = executionEngine;
		this.linkResolver = linkResolver;
		this.mirrorFactory = mirrorFactory;
		this.objectSynchronizer = objectSynchronizer;
		this.bootClassFinder = bootClassFinder;
		this.properties = properties;
		this.env = env;
		reflection = new Reflection(threadManager);
		operations = new VMOperations(this);
	}

	/**
	 * @throws IllegalStateException If VM is not initialized.
	 */
	public void assertInitialized() {
		InitializationState state = this.state.get();
		if (state == InitializationState.UNINITIALIZED || state == InitializationState.FAILED) {
			throw new IllegalStateException("VM is not initialized");
		}
	}

	/**
	 * @throws IllegalStateException If VM is not booted.
	 */
	public void assertBooted() {
		if (state.get() != InitializationState.BOOTED) {
			throw new IllegalStateException("VM is not booted");
		}
	}

	/**
	 * Initializes the VM.
	 *
	 * @throws IllegalStateException If VM fails to transit to {@link InitializationState#INITIALIZING} state,
	 *                               or fails to initialize.
	 */
	public void initialize() {
		//<editor-fold desc="VM initialization">
		if (state.compareAndSet(InitializationState.UNINITIALIZED, InitializationState.INITIALIZING)) {
			init();
		} else {
			throw new IllegalStateException("Failed to enter in INITIALIZING state");
		}
		//</editor-fold>
	}

	/**
	 * Full VM initialization.
	 * After this method is called, caller thread will remain attached.
	 *
	 * @throws IllegalStateException If VM fails to transit to {@link InitializationState#BOOTING} state,
	 *                               or fails to boot.
	 */
	public void bootstrap() {
		//<editor-fold desc="VM bootstrap">
		tryInitialize();
		assertInitialized();
		//<editor-fold desc="VM initialization">
		if (state.compareAndSet(InitializationState.INITIALIZED, InitializationState.BOOTING)) {
			boot();
		} else {
			throw new IllegalStateException("Failed to enter in BOOTING state");
		}
		//</editor-fold>
	}

	/**
	 * @return current initialization state.
	 */
	public InitializationState getState() {
		return state.get();
	}

	/**
	 * @inheritDoc
	 */
	public MemoryAllocator getMemoryAllocator() {
		return memoryAllocator;
	}

	/**
	 * @inheritDoc
	 */
	public MemoryManager getMemoryManager() {
		return memoryManager;
	}

	/**
	 * @return VM interface.
	 */
	public VMInterface getInterface() {
		return vmInterface;
	}

	/**
	 * @return VM helper.
	 */
	public Symbols getSymbols() {
		return symbols;
	}

	/**
	 * @return VM primitives.
	 */
	public Primitives getPrimitives() {
		return primitives;
	}

	/**
	 * @return VM operations.
	 */
	public VMOperations getOperations() {
		return operations;
	}

	/**
	 * @inheritDoc
	 */
	public LinkResolver getLinkResolver() {
		return linkResolver;
	}

	/**
	 * @return Reflection helper.
	 */
	public Reflection getReflection() {
		return reflection;
	}

	/**
	 * @inheritDoc
	 */
	public ClassDefiner getClassDefiner() {
		return classDefiner;
	}

	/**
	 * @inheritDoc
	 */
	public ThreadManager getThreadManager() {
		return threadManager;
	}

	/**
	 * @inheritDoc
	 */
	public FileDescriptorManager getFileDescriptorManager() {
		return fileDescriptorManager;
	}

	/**
	 * @inheritDoc
	 */
	public NativeLibraryManager getNativeLibraryManager() {
		return nativeLibraryManager;
	}

	/**
	 * @inheritDoc
	 */
	public StringPool getStringPool() {
		return stringPool;
	}

	/**
	 * @inheritDoc
	 */
	public ManagementInterface getManagementInterface() {
		return managementInterface;
	}

	/**
	 * @inheritDoc
	 */
	public TimeManager getTimeManager() {
		return timeManager;
	}

	/**
	 * @inheritDoc
	 */
	public ClassLoaders getClassLoaders() {
		return classLoaders;
	}

	/**
	 * @inheritDoc
	 */
	public ExecutionEngine getExecutionEngine() {
		return executionEngine;
	}

	/**
	 * @inheritDoc
	 */
	public MirrorFactory getMirrorFactory() {
		return mirrorFactory;
	}

	/**
	 * @inheritDoc
	 */
	public ObjectSynchronizer getObjectSynchronizer() {
		return objectSynchronizer;
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public BootClassFinder getBootClassFinder() {
		return bootClassFinder;
	}

	/**
	 * @inheritDoc
	 */
	public Map<String, String> getProperties() {
		return properties;
	}

	/**
	 * @inheritDoc
	 */
	public Map<String, String> getenv() {
		return env;
	}

	/**
	 * Returns system thread group.
	 *
	 * @return system thread group.
	 */
	public InstanceValue getSystemThreadGroup() {
		return systemThreadGroup;
	}

	/**
	 * Returns main thread group.
	 *
	 * @return system thread group.
	 */
	public InstanceValue getMainThreadGroup() {
		return mainThreadGroup;
	}

	/**
	 * Returns thread storage.
	 *
	 * @return thread storage.
	 */
	public ThreadStorage getThreadStorage() {
		return threadManager.currentThreadStorage();
	}

	/**
	 * Returns current Java thread.
	 *
	 * @return current Java thread.
	 */
	public JavaThread currentJavaThread() {
		return threadManager.currentJavaThread();
	}

	/**
	 * Returns current OS thread.
	 *
	 * @return current OS thread.
	 */
	public OSThread currentOSThread() {
		return threadManager.currentOsThread();
	}

	/**
	 * Searches for bootstrap class.
	 *
	 * @param name       Name of the class.
	 * @param initialize True if class should be initialized if found.
	 * @return bootstrap class or {@code null}, if not found.
	 */
	public JavaClass findBootstrapClass(String name, boolean initialize) {
		return operations.findClass(memoryManager.nullValue(), name, initialize);
	}

	/**
	 * Searches for bootstrap class.
	 *
	 * @param name Name of the class.
	 * @return bootstrap class or {@code null}, if not found.
	 */
	public JavaClass findBootstrapClass(String name) {
		return findBootstrapClass(name, false);
	}

	private void init() {
		ThreadManager threadManager = this.threadManager;
		try {
			ClassLoaders classLoaders = this.classLoaders;
			VMOperations ops = this.operations;
			// This is essentially the same hack HotSpot does when VM is starting up
			// https://github.com/openjdk/jdk/blob/8ecdaa68111f2e060a3f46a5cf6f2ba95c9ebad1/src/hotspot/share/memory/universe.cpp#L480
			// java/lang/Object & java/lang/Class must be loaded manually,
			// otherwise some MemoryManager implementations will bottleneck.
			InstanceClass klass = internalLink("java/lang/Class");
			InstanceClass object = internalLink("java/lang/Object");
			ops.link(klass);
			ops.link(object);
			classLoaders.initializeBootOop(klass, klass);
			classLoaders.initializeBootOop(object, klass);
			NativeJava.injectPhase2(this);
			InitializedSymbols initializedVMSymbols = new InitializedSymbols(this);
			((DelegatingSymbols) symbols).setSymbols(initializedVMSymbols);
			symbols = initializedVMSymbols;
			InitializedPrimitives initializedVMPrimitives = new InitializedPrimitives(mirrorFactory);
			((DelegatingPrimitives) primitives).setPrimitives(initializedVMPrimitives);
			primitives = initializedVMPrimitives;
			NativeJava.init(this);
			threadManager.attachCurrentThread();
			InstanceClass groupClass = symbols.java_lang_ThreadGroup();
			ops.initialize(groupClass);
			ThreadStorage ts = currentOSThread().getStorage();
			// Initialize system group
			InstanceValue sysGroup = memoryManager.newInstance(groupClass);
			{
				JavaMethod init = linkResolver.resolveSpecialMethod(groupClass, "<init>", "()V");
				Locals locals = ts.newLocals(init);
				locals.setReference(0, sysGroup);
				ops.invokeVoid(init, locals);
			}
			systemThreadGroup = sysGroup;
			// Initialize main group
			InstanceValue mainGroup = memoryManager.newInstance(groupClass);
			{
				JavaMethod init = linkResolver.resolveSpecialMethod(groupClass, "<init>", "(Ljava/lang/ThreadGroup;Ljava/lang/String;)V");
				Locals locals = ts.newLocals(init);
				locals.setReference(0, mainGroup);
				locals.setReference(1, sysGroup);
				locals.setReference(2, ops.newUtf8("main"));
				ops.invokeVoid(init, locals);
			}
			mainThreadGroup = mainGroup;
			IntrinsicsNatives.init(this);
			state.set(InitializationState.INITIALIZED);
		} catch (Exception ex) {
			state.set(InitializationState.FAILED);
			throw new IllegalStateException("VM initialization failed", ex);
		} finally {
			threadManager.detachCurrentThread();
		}
	}

	private void tryInitialize() {
		if (state.compareAndSet(InitializationState.UNINITIALIZED, InitializationState.INITIALIZING)) {
			init();
		}
	}

	private void boot() {
		//<editor-fold desc="VM bootstrap">
		try {
			Symbols symbols = this.symbols;
			ThreadManager threadManager = this.threadManager;
			threadManager.attachCurrentThread();
			VMOperations ops = operations;
			ops.initialize(symbols.java_lang_ClassLoader());
			InstanceClass sysClass = symbols.java_lang_System();

			// Inject unsafe constants
			// This must be done first, otherwise
			// jdk/internal/misc/Unsafe will cache wrong values
			InstanceClass unsafeConstants = (InstanceClass) findBootstrapClass("jdk/internal/misc/UnsafeConstants", true);
			if (unsafeConstants != null) {
				MemoryAllocator memoryAllocator = this.memoryAllocator;
				ops.initialize(unsafeConstants);
				ops.putInt(unsafeConstants, "ADDRESS_SIZE", memoryAllocator.addressSize());
				ops.putInt(unsafeConstants, "PAGE_SIZE", memoryAllocator.pageSize());
				ops.putBoolean(unsafeConstants, "BIG_ENDIAN", memoryAllocator.getByteOrder() == ByteOrder.BIG_ENDIAN);
			}
			LinkResolver linkResolver = this.linkResolver;
			ThreadStorage ts = getThreadStorage();
			ops.initialize(sysClass);
			{
				InstanceValue threadGroup = mainThreadGroup;
				InstanceValue oop = currentJavaThread().getOop();
				ops.putReference(oop, "group", "Ljava/lang/ThreadGroup;", threadGroup);
				JavaMethod add = linkResolver.resolveVirtualMethod(threadGroup, "add", "(Ljava/lang/Thread;)V");
				Locals locals = ts.newLocals(add);
				locals.setReference(0, threadGroup);
				locals.setReference(1, oop);
				ops.invokeVoid(add, locals);
			}
			findBootstrapClass("java/lang/reflect/Method", true);
			findBootstrapClass("java/lang/reflect/Field", true);
			findBootstrapClass("java/lang/reflect/Constructor", true);

			JavaMethod initializeSystemClass = sysClass.getMethod("initializeSystemClass", "()V");
			if (initializeSystemClass != null) {
				// pre JDK 9 boot
				ops.invokeVoid(initializeSystemClass, ts.newLocals(initializeSystemClass));
			} else {
				findBootstrapClass("java/lang/StringUTF16", true);
				{
					JavaMethod initPhase1 = linkResolver.resolveStaticMethod(sysClass, "initPhase1", "()V");
					ops.invokeVoid(initPhase1, ts.newLocals(initPhase1));
				}
				findBootstrapClass("java/lang/invoke/MethodHandle", true);
				findBootstrapClass("java/lang/invoke/ResolvedMethodName", true);
				findBootstrapClass("java/lang/invoke/MemberName", true);
				findBootstrapClass("java/lang/invoke/MethodHandleNatives", true);

				int result;
				{
					JavaMethod initPhase2 = linkResolver.resolveStaticMethod(sysClass, "initPhase2", "(ZZ)I");
					Locals locals = ts.newLocals(initPhase2);
					locals.setInt(0, 1);
					locals.setInt(1, 1);
					result = ops.invokeInt(initPhase2, locals);
				}
				if (result != 0) {
					throw new IllegalStateException("VM bootstrapping failed, initPhase2 returned " + result);
				}
				{
					JavaMethod initPhase3 = linkResolver.resolveStaticMethod(sysClass, "initPhase3", "()V");
					ops.invokeVoid(initPhase3, ts.newLocals(initPhase3));
				}
			}
			{
				JavaMethod getSystemClassLoader = linkResolver.resolveStaticMethod(symbols.java_lang_ClassLoader(), "getSystemClassLoader", "()Ljava/lang/ClassLoader;");
				ops.invokeVoid(getSystemClassLoader, ts.newLocals(getSystemClassLoader));
			}
			state.set(InitializationState.BOOTED);
		} catch (Exception ex) {
			state.set(InitializationState.FAILED);
			throw new IllegalStateException("VM bootstrap failed", ex);
		}
		//</editor-fold>
	}

	private InstanceClass internalLink(String name) {
		ParsedClassData result = bootClassFinder.findBootClass(name);
		if (result == null) {
			throw new IllegalStateException("Bootstrap class not found: " + name);
		}
		return (InstanceClass) operations.defineClass(memoryManager.nullValue(), result, memoryManager.nullValue(), "JVM_DefineClass", true);
	}
}
