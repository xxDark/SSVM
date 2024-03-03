package dev.xdark.ssvm;

import dev.xdark.ssvm.api.SimpleVMInterface;
import dev.xdark.ssvm.api.VMInterface;
import dev.xdark.ssvm.classloading.BootClassFinder;
import dev.xdark.ssvm.classloading.ClassDefiner;
import dev.xdark.ssvm.classloading.ClassLoaderData;
import dev.xdark.ssvm.classloading.ClassLoaders;
import dev.xdark.ssvm.classloading.ClassStorage;
import dev.xdark.ssvm.classloading.ParsedClassData;
import dev.xdark.ssvm.classloading.RuntimeBootClassFinder;
import dev.xdark.ssvm.classloading.SimpleClassDefiner;
import dev.xdark.ssvm.classloading.SimpleClassLoaders;
import dev.xdark.ssvm.classloading.SimpleClassStorage;
import dev.xdark.ssvm.execution.*;
import dev.xdark.ssvm.filesystem.FileManager;
import dev.xdark.ssvm.filesystem.SimpleFileManager;
import dev.xdark.ssvm.inject.InjectedClassLayout;
import dev.xdark.ssvm.jni.NativeLibraryManager;
import dev.xdark.ssvm.jni.SimpleNativeLibraryManager;
import dev.xdark.ssvm.jvm.ManagementInterface;
import dev.xdark.ssvm.jvm.SimpleManagementInterface;
import dev.xdark.ssvm.jvmti.JVMTIEnv;
import dev.xdark.ssvm.jvmti.VMEventCollection;
import dev.xdark.ssvm.memory.allocation.MemoryAllocator;
import dev.xdark.ssvm.memory.allocation.NavigableMemoryAllocator;
import dev.xdark.ssvm.memory.management.MemoryManager;
import dev.xdark.ssvm.memory.management.SimpleMemoryManager;
import dev.xdark.ssvm.memory.management.SimpleStringPool;
import dev.xdark.ssvm.memory.management.StringPool;
import dev.xdark.ssvm.mirror.MirrorFactory;
import dev.xdark.ssvm.mirror.SimpleMirrorFactory;
import dev.xdark.ssvm.mirror.member.JavaMethod;
import dev.xdark.ssvm.mirror.type.InstanceClass;
import dev.xdark.ssvm.mirror.type.JavaClass;
import dev.xdark.ssvm.natives.IntrinsicsNatives;
import dev.xdark.ssvm.operation.VMOperations;
import dev.xdark.ssvm.symbol.Primitives;
import dev.xdark.ssvm.symbol.Symbols;
import dev.xdark.ssvm.synchronizer.ObjectSynchronizer;
import dev.xdark.ssvm.synchronizer.java.LockObjectSynchronizer;
import dev.xdark.ssvm.thread.JavaThread;
import dev.xdark.ssvm.thread.OSThread;
import dev.xdark.ssvm.thread.ThreadManager;
import dev.xdark.ssvm.thread.ThreadStorage;
import dev.xdark.ssvm.thread.virtual.VirtualThreadManager;
import dev.xdark.ssvm.timezone.SimpleTimeManager;
import dev.xdark.ssvm.timezone.TimeManager;
import dev.xdark.ssvm.util.CloseableLock;
import dev.xdark.ssvm.util.Reflection;
import dev.xdark.ssvm.value.InstanceValue;
import lombok.experimental.Delegate;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteOrder;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class VirtualMachine implements VMEventCollection {

	private final AtomicReference<InitializationState> state = new AtomicReference<>(InitializationState.UNINITIALIZED);
	private final VMInterface vmInterface;
	private final MemoryAllocator memoryAllocator;
	private final ObjectSynchronizer objectSynchronizer;
	private final MemoryManager memoryManager;
	private final ClassDefiner classDefiner;
	private final ThreadManager threadManager;
	private final FileManager fileManager;
	private final NativeLibraryManager nativeLibraryManager;
	private final TimeManager timeManager;
	private final ManagementInterface managementInterface;
	private final StringPool stringPool;
	private final ClassLoaders classLoaders;
	private final ExecutionEngine executionEngine;
	private final MirrorFactory mirrorFactory;
	private final BootClassFinder bootClassFinder;
	private final ClassStorage classStorage;
	private final LinkResolver linkResolver;
	private final RuntimeResolver runtimeResolver;
	private final Map<String, String> properties;
	private final Map<String, String> env;
	private final Reflection reflection;
	@Delegate(types = VMEventCollection.class)
	private final JVMTI jvmti;
	private final VMOperations operations;
	private Symbols symbols;
	private Primitives primitives;
	private volatile InstanceValue systemThreadGroup;
	private volatile InstanceValue mainThreadGroup;

	public VirtualMachine() {
		this(null);
	}

	/**
	 * Constructor for running some logic operating on {@code this}
	 * before the VM internals are initialized.
	 * <p/>
	 * This allows sub-classes to set field values <i>(though non-final)</i>
	 * which can then be used in the {@code createX} method implementations.
	 * The consumer allows us to act on {@code this} <i>"before the super
	 * constructor call".</i>
	 *
	 * @param consumer
	 * 		Consumer to run before the VM internals are initialized.
	 * @param <V>
	 * 		Self type.
	 */
	@SuppressWarnings("unchecked")
	public <V extends VirtualMachine> VirtualMachine(Consumer<V> consumer) {
		if (consumer != null) consumer.accept((V) this);
		properties = createSystemProperties();
		env = createEnvironmentVariables();
		vmInterface = createVMInterface();
		DelegatingSymbols delegatingSymbols = new DelegatingSymbols();
		delegatingSymbols.setSymbols(new UninitializedSymbols(this));
		symbols = delegatingSymbols;
		DelegatingPrimitives delegatingPrimitives = new DelegatingPrimitives();
		delegatingPrimitives.setPrimitives(new UninitializedPrimitives());
		primitives = delegatingPrimitives;
		memoryAllocator = createMemoryAllocator();
		objectSynchronizer = createObjectSynchronizer();
		memoryManager = createMemoryManager();
		classDefiner = createClassDefiner();
		threadManager = createThreadManager();
		fileManager = createFileManager();
		nativeLibraryManager = createNativeLibraryManager();
		timeManager = createTimeManager();
		managementInterface = createManagementInterface();
		stringPool = createStringPool();
		classLoaders = createClassLoaders();
		executionEngine = createExecutionEngine();
		mirrorFactory = createMirrorFactory();
		bootClassFinder = createBootClassFinder();
		classStorage = createClassStorage();
		linkResolver = new LinkResolver(this);
		runtimeResolver = new RuntimeResolver(this, linkResolver);
		reflection = new Reflection(this);
		jvmti = new JVMTI(this);
		operations = new VMOperations(this);
	}

	protected VMInterface createVMInterface() {
		return new SimpleVMInterface();
	}

	protected MemoryAllocator createMemoryAllocator() {
		return new NavigableMemoryAllocator();
	}

	protected ObjectSynchronizer createObjectSynchronizer() {
		return new LockObjectSynchronizer();
	}

	protected MemoryManager createMemoryManager() {
		return new SimpleMemoryManager(this);
	}

	protected ClassDefiner createClassDefiner() {
		return new SimpleClassDefiner();
	}

	protected ThreadManager createThreadManager() {
		return new VirtualThreadManager(this);
	}

	protected FileManager createFileManager() {
		return new SimpleFileManager();
	}

	protected NativeLibraryManager createNativeLibraryManager() {
		return new SimpleNativeLibraryManager();
	}

	protected TimeManager createTimeManager() {
		return new SimpleTimeManager();
	}

	protected ManagementInterface createManagementInterface() {
		return new SimpleManagementInterface();
	}

	protected StringPool createStringPool() {
		return new SimpleStringPool(this);
	}

	protected ClassLoaders createClassLoaders() {
		return new SimpleClassLoaders(this);
	}

	protected ExecutionEngine createExecutionEngine() {
		return new SimpleExecutionEngine(this);
	}

	protected MirrorFactory createMirrorFactory() {
		return new SimpleMirrorFactory(this);
	}

	protected BootClassFinder createBootClassFinder() {
		return RuntimeBootClassFinder.create();
	}

	protected ClassStorage createClassStorage() {
		return new SimpleClassStorage(this);
	}

	protected Map<String, String> createSystemProperties() {
		//noinspection unchecked
		return new LinkedHashMap<>((Map<String, String>) (Map) System.getProperties());
	}

	protected Map<String, String> createEnvironmentVariables() {
		TreeMap<String, String> env = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		env.putAll(System.getenv());
		return env;
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
		if (state.compareAndSet(InitializationState.UNINITIALIZED, InitializationState.INITIALIZING)) {
			init();
		} else {
			throw new IllegalStateException("Failed to enter in INITIALIZING state");
		}
	}

	/**
	 * Full VM initialization.
	 * After this method is called, caller thread will remain attached.
	 *
	 * @throws IllegalStateException If VM fails to transit to {@link InitializationState#BOOTING} state,
	 *                               or fails to boot.
	 */
	public void bootstrap() {
		tryInitialize();
		assertInitialized();
		if (state.compareAndSet(InitializationState.INITIALIZED, InitializationState.BOOTING)) {
			boot();
		} else {
			throw new IllegalStateException("Failed to enter in BOOTING state");
		}
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
	 * @return New JVMTI environment.
	 */
	public JVMTIEnv newJvmtiEnv() {
		return jvmti.create();
	}

	/**
	 * Allows for modifying {@link Stack} values created by the VM.
	 * <p/>
	 * To be used in {@link ThreadStorage} implementations.
	 *
	 * @return Decorator for created stacks.
	 */
	public @Nullable StackDecorator getStackDecorator() {
		return null;
	}

	/**
	 * Allows for modifying {@link Locals} values created by the VM.
	 * <p/>
	 * To be used in {@link ThreadStorage} implementations.
	 *
	 * @return Decorator for created locals.
	 */
	public @Nullable LocalsDecorator getLocalDecorator() {
		return null;
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
	 * @return Link resolver.
	 */
	public LinkResolver getLinkResolver() {
		return linkResolver;
	}

	/**
	 * @return Runtime resolver.
	 */
	public RuntimeResolver getRuntimeResolver() {
		return runtimeResolver;
	}

	/**
	 * @return Reflection helper.
	 */
	public Reflection getReflection() {
		return reflection;
	}

	/**
	 * @return Class definer.
	 */
	public ClassDefiner getClassDefiner() {
		return classDefiner;
	}

	/**
	 * @return Thread manager.
	 */
	public ThreadManager getThreadManager() {
		return threadManager;
	}

	/**
	 * @return File manager.
	 */
	public FileManager getFileManager() {
		return fileManager;
	}

	/**
	 * @return Native library manager.
	 */
	public NativeLibraryManager getNativeLibraryManager() {
		return nativeLibraryManager;
	}

	/**
	 * @return Stirng pool.
	 */
	public StringPool getStringPool() {
		return stringPool;
	}

	/**
	 * @return Management interface.
	 * TODO: remove?
	 */
	public ManagementInterface getManagementInterface() {
		return managementInterface;
	}

	/**
	 * @return Time manager.
	 */
	public TimeManager getTimeManager() {
		return timeManager;
	}

	/**
	 * @return Class loaders storage.
	 */
	public ClassLoaders getClassLoaders() {
		return classLoaders;
	}

	/**
	 * @return Executing engine.
	 */
	public ExecutionEngine getExecutionEngine() {
		return executionEngine;
	}

	/**
	 * @return Mirror factory.
	 */
	public MirrorFactory getMirrorFactory() {
		return mirrorFactory;
	}

	/**
	 * @return Object synchronizer.
	 */
	public ObjectSynchronizer getObjectSynchronizer() {
		return objectSynchronizer;
	}

	/**
	 * @return Boot class finder.
	 */
	public BootClassFinder getBootClassFinder() {
		return bootClassFinder;
	}

	/**
	 * @return Class storage.
	 */
	public ClassStorage getClassStorage() {
		return classStorage;
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
	 * This method should not be used by user code.
	 *
	 * @param name       Name of the class.
	 * @param initialize True if class should be initialized if found.
	 * @return bootstrap class or {@code null}, if not found.
	 */
	@Nullable
	public JavaClass findBootstrapClass(String name, boolean initialize) {
		return operations.findBootstrapClassOrNull(name, initialize);
	}

	/**
	 * Searches for bootstrap class.
	 *
	 * @param name Name of the class.
	 * @return bootstrap class or {@code null}, if not found.
	 */
	@Nullable
	public JavaClass findBootstrapClass(String name) {
		return findBootstrapClass(name, false);
	}

	/**
	 * @return JVM version.
	 */
	public int getJvmVersion() {
		String classFileVersionProperty = getProperties().get("java.class.version");
		if (classFileVersionProperty.contains("."))
			classFileVersionProperty = classFileVersionProperty.substring(0, classFileVersionProperty.indexOf('.'));
		return Integer.parseInt(classFileVersionProperty) - 44;
	}

	private void init() {
		ThreadManager threadManager = this.threadManager;
		try {
			// Create temporary JVMTI environments here to hook into some bootstrap classes
			NativeJava.jvmtiPrepare(this);

			ClassLoaders classLoaders = this.classLoaders;
			VMOperations ops = this.operations;
			// This is essentially the same hack HotSpot does when VM is starting up
			// https://github.com/openjdk/jdk/blob/8ecdaa68111f2e060a3f46a5cf6f2ba95c9ebad1/src/hotspot/share/memory/universe.cpp#L480
			MemoryManager memoryManager = this.memoryManager;
			ClassLoaderData data = classLoaders.createClassLoaderData(memoryManager.nullValue());
			InstanceClass jlc = internalLink("java/lang/Class");
			internalLink("java/lang/Object");
			// After we link both, we need to fix all classes who have no mirrors
			// No classes from system class loader are loaded at this point
			try (CloseableLock lock = data.lock()) {
				long offset = jlc.getField(
					InjectedClassLayout.java_lang_Class_id.name(),
					"I"
				).getOffset();
				// Firstly fix java/lang/Class,
				memoryManager.newJavaLangClass(jlc);
				jlc.getOop().getData().writeInt(offset, jlc.getId());
				// then the rest
				for (InstanceClass klass : data.list()) {
					InstanceValue oop = klass.getOop();
					if (oop == null) {
						oop = memoryManager.newClassOop(klass);
						klass.setOop(oop);
						oop.getData().writeInt(offset, klass.getId());
					}
				}
			}
			InitializedSymbols initializedVMSymbols = new InitializedSymbols(this);
			((DelegatingSymbols) symbols).setSymbols(initializedVMSymbols);
			symbols = initializedVMSymbols;
			InitializedPrimitives initializedVMPrimitives = new InitializedPrimitives(mirrorFactory);
			((DelegatingPrimitives) primitives).setPrimitives(initializedVMPrimitives);
			primitives = initializedVMPrimitives;
			// Post-initialization for initializedVMSymbols
			// There are some types that have different names on different
			// JDKs, we need to be able to catch VM exceptions at this point.
			NativeJava.setInstructions(this);
			threadManager.attachCurrentThread();
			initializedVMSymbols.postInit(this);
			NativeJava.initialization(this);
			NativeJava.postInitialization(this);
			InstanceClass groupClass = symbols.java_lang_ThreadGroup();
			ops.initialize(groupClass);
			ThreadStorage ts = currentOSThread().getStorage();
			// Initialize system group
			InstanceValue sysGroup = memoryManager.newInstance(groupClass);
			{
				JavaMethod init = linkResolver.resolveVirtualMethod(groupClass, "<init>", "()V");
				Locals locals = ts.newLocals(init);
				locals.setReference(0, sysGroup);
				ops.invokeVoid(init, locals);
			}
			systemThreadGroup = sysGroup;
			// Initialize main group
			InstanceValue mainGroup = memoryManager.newInstance(groupClass);
			{
				JavaMethod init = linkResolver.resolveVirtualMethod(groupClass, "<init>", "(Ljava/lang/ThreadGroup;Ljava/lang/String;)V");
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
				if (unsafeConstants.getField("ADDRESS_SIZE", "I") != null)
					ops.putInt(unsafeConstants, "ADDRESS_SIZE", memoryAllocator.addressSize());
				else if (unsafeConstants.getField("ADDRESS_SIZE0", "I") != null) // Renamed in later JDK's
					ops.putInt(unsafeConstants, "ADDRESS_SIZE0", memoryAllocator.addressSize());
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
				JavaMethod add = linkResolver.resolveVirtualMethod(threadGroup.getJavaClass(), "add", "(Ljava/lang/Thread;)V");
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
		// Loading java/lang/Class may be enough
		// to trigger code that will load other classes for us.
		ClassLoaderData data = classLoaders.getClassLoaderData(memoryManager.nullValue());
		InstanceClass klass = data.getClass(name);
		if (klass != null) {
			return klass;
		}
		ParsedClassData result = bootClassFinder.findBootClass(name);
		if (result == null) {
			throw new PanicException("Bootstrap class not found: " + name);
		}
		return operations.defineClass(memoryManager.nullValue(), result, memoryManager.nullValue(), "JVM_DefineClass");
	}
}
