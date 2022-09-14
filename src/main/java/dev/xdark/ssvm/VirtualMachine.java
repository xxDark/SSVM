package dev.xdark.ssvm;

import dev.xdark.ssvm.api.VMInterface;
import dev.xdark.ssvm.classloading.BootClassLoader;
import dev.xdark.ssvm.classloading.ClassDefiner;
import dev.xdark.ssvm.classloading.ClassLoaderData;
import dev.xdark.ssvm.classloading.ClassLoaders;
import dev.xdark.ssvm.classloading.ClassParseResult;
import dev.xdark.ssvm.classloading.RuntimeBootClassLoader;
import dev.xdark.ssvm.classloading.SimpleClassDefiner;
import dev.xdark.ssvm.classloading.SimpleClassLoaders;
import dev.xdark.ssvm.execution.ExecutionEngine;
import dev.xdark.ssvm.execution.Locals;
import dev.xdark.ssvm.execution.NoopSafePoint;
import dev.xdark.ssvm.execution.SafePoint;
import dev.xdark.ssvm.execution.SimpleExecutionEngine;
import dev.xdark.ssvm.fs.FileDescriptorManager;
import dev.xdark.ssvm.fs.SimpleFileDescriptorManager;
import dev.xdark.ssvm.jvm.ManagementInterface;
import dev.xdark.ssvm.jvm.SimpleManagementInterface;
import dev.xdark.ssvm.memory.allocation.MemoryAllocator;
import dev.xdark.ssvm.memory.allocation.NavigableMemoryAllocator;
import dev.xdark.ssvm.memory.management.MemoryManager;
import dev.xdark.ssvm.memory.management.SimpleMemoryManager;
import dev.xdark.ssvm.memory.management.SimpleStringPool;
import dev.xdark.ssvm.memory.management.StringPool;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.mirror.JavaClass;
import dev.xdark.ssvm.mirror.JavaMethod;
import dev.xdark.ssvm.mirror.MirrorFactory;
import dev.xdark.ssvm.mirror.SimpleMirrorFactory;
import dev.xdark.ssvm.natives.IntrinsicsNatives;
import dev.xdark.ssvm.nt.NativeLibraryManager;
import dev.xdark.ssvm.nt.SimpleNativeLibraryManager;
import dev.xdark.ssvm.symbol.InitializedVMPrimitives;
import dev.xdark.ssvm.symbol.InitializedVMSymbols;
import dev.xdark.ssvm.symbol.UninitializedVMPrimitives;
import dev.xdark.ssvm.symbol.UninitializedVMSymbols;
import dev.xdark.ssvm.symbol.VMPrimitives;
import dev.xdark.ssvm.symbol.VMSymbols;
import dev.xdark.ssvm.thread.OSThread;
import dev.xdark.ssvm.thread.ThreadManager;
import dev.xdark.ssvm.thread.ThreadStorage;
import dev.xdark.ssvm.thread.JavaThread;
import dev.xdark.ssvm.thread.virtual.VirtualThreadManager;
import dev.xdark.ssvm.tz.SimpleTimeManager;
import dev.xdark.ssvm.tz.TimeManager;
import dev.xdark.ssvm.util.DefaultVMOperations;
import dev.xdark.ssvm.util.InvokeDynamicLinker;
import dev.xdark.ssvm.util.Reflection;
import dev.xdark.ssvm.util.VMHelper;
import dev.xdark.ssvm.util.VMOperations;
import dev.xdark.ssvm.value.InstanceValue;
import dev.xdark.ssvm.value.JavaValue;
import dev.xdark.ssvm.value.ObjectValue;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class VirtualMachine {

	private final AtomicReference<InitializationState> state = new AtomicReference<>(InitializationState.UNINITIALIZED);
	private final BootClassLoaderHolder bootClassLoader;
	private final VMInterface vmInterface;
	private final MemoryAllocator memoryAllocator;
	private final MemoryManager memoryManager;
	private VMSymbols symbols;
	private VMPrimitives primitives;
	private final VMHelper helper;
	private final ClassDefiner classDefiner;
	private final ThreadManager threadManager;
	private final FileDescriptorManager fileDescriptorManager;
	private final NativeLibraryManager nativeLibraryManager;
	private final StringPool stringPool;
	private final ManagementInterface managementInterface;
	private final TimeManager timeManager;
	private final ClassLoaders classLoaders;
	private final Map<String, String> properties;
	private final Map<String, String> env;
	private final VMInitializer initializer;
	private final ExecutionEngine executionEngine;
	private final SafePoint safePoint;
	private final VMOperations publicOperations;
	private final VMOperations trustedOperations;
	private final LinkResolver publicLinkResolver;
	private final LinkResolver trustedLinkResolver;
	private final InvokeDynamicLinker invokeDynamicLinker;
	private final Reflection reflection;
	private final MirrorFactory mirrorFactory;
	private volatile InstanceValue systemThreadGroup;
	private volatile InstanceValue mainThreadGroup;

	public VirtualMachine(VMInitializer initializer) {
		this.initializer = initializer;
		ClassLoaders classLoaders = createClassLoaders();
		this.classLoaders = classLoaders;
		memoryAllocator = createMemoryAllocator();
		safePoint = createSafePoint();
		mirrorFactory = createMirrorFactory();
		memoryManager = createMemoryManager();
		bootClassLoader = new BootClassLoaderHolder(this, createBootClassLoader(), classLoaders.setClassLoaderData(memoryManager.nullValue()));
		vmInterface = new VMInterface();
		helper = new VMHelper(this);
		threadManager = createThreadManager();
		classDefiner = createClassDefiner();
		DelegatingVMSymbols symbols = new DelegatingVMSymbols();
		symbols.setSymbols(new UninitializedVMSymbols(this));
		this.symbols = symbols;
		DelegatingVMPrimitives primitives = new DelegatingVMPrimitives();
		primitives.setPrimitives(new UninitializedVMPrimitives());
		this.primitives = primitives;
		fileDescriptorManager = createFileDescriptorManager();
		nativeLibraryManager = createNativeLibraryManager();
		stringPool = createStringPool();
		managementInterface = createManagementInterface();
		timeManager = createTimeManager();
		executionEngine = createExecutionEngine();
		publicLinkResolver = new LinkResolver(this, false);
		trustedLinkResolver = new LinkResolver(this, true);
		invokeDynamicLinker = new InvokeDynamicLinker(this);
		reflection = new Reflection(this);
		publicOperations = createPublicOperations();
		trustedOperations = createTrustedOperations();

		properties = System.getProperties().entrySet().stream().collect(Collectors.toMap(x -> x.getKey().toString(), x -> x.getValue().toString()));
		env = new HashMap<>(System.getenv());
	}

	public VirtualMachine() {
		this(DummyVMInitializer.INSTANCE);
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
	 * This must be only invoked by the VM
	 * to support snapshot restore.
	 *
	 * @param state New state.
	 */
	public void setState(InitializationState state) {
		this.state.set(state);
	}

	/**
	 * Returns properties that will be used
	 * for initialization.
	 *
	 * @return system properties.
	 */
	public Map<String, String> getProperties() {
		return properties;
	}

	/**
	 * Returns process environment variables that will be used
	 * for initialization.
	 *
	 * @return environment variables.
	 */
	public Map<String, String> getenv() {
		return env;
	}

	/**
	 * Returns memory allocator.
	 *
	 * @return memory allocator.
	 */
	public MemoryAllocator getMemoryAllocator() {
		return memoryAllocator;
	}

	/**
	 * Returns memory manager.
	 *
	 * @return memory manager.
	 */
	public MemoryManager getMemoryManager() {
		return memoryManager;
	}

	/**
	 * Returns VM interface.
	 *
	 * @return VM interface.
	 */
	public VMInterface getInterface() {
		return vmInterface;
	}

	/**
	 * Returns VM symbols.
	 *
	 * @return VM symbols.
	 */
	public VMSymbols getSymbols() {
		return symbols;
	}

	/**
	 * Returns VM primitives.
	 *
	 * @return VM primitives.
	 */
	public VMPrimitives getPrimitives() {
		return primitives;
	}

	/**
	 * Returns VM operations.
	 *
	 * @return VM operations.
	 */
	public VMOperations getPublicOperations() {
		return publicOperations;
	}

	/**
	 * Returns VM operations.
	 *
	 * @return VM operations.
	 */
	public VMOperations getTrustedOperations() {
		return trustedOperations;
	}

	/**
	 * Returns link resolver.
	 *
	 * @return link resolver.
	 */
	public LinkResolver getPublicLinkResolver() {
		return publicLinkResolver;
	}

	/**
	 * Returns link resolver.
	 *
	 * @return link resolver.
	 */
	public LinkResolver getTrustedLinkResolver() {
		return trustedLinkResolver;
	}

	/**
	 * Returns invokedynamic linker.
	 *
	 * @return invokedynamic linker.
	 */
	public InvokeDynamicLinker getInvokeDynamicLinker() {
		return invokeDynamicLinker;
	}

	/**
	 * Returns reflection helper.
	 *
	 * @return reflection helper.
	 */
	public Reflection getReflection() {
		return reflection;
	}

	/**
	 * Returns VM helper.
	 *
	 * @return VM helper.
	 */
	public VMHelper getHelper() {
		return helper;
	}

	/**
	 * Returns class definer.
	 *
	 * @return class definer.
	 */
	public ClassDefiner getClassDefiner() {
		return classDefiner;
	}

	/**
	 * Returns thread manager.
	 *
	 * @return thread manager.
	 */
	public ThreadManager getThreadManager() {
		return threadManager;
	}

	/**
	 * Returns file descriptor manager.
	 *
	 * @return file descriptor manager.
	 */
	public FileDescriptorManager getFileDescriptorManager() {
		return fileDescriptorManager;
	}

	/**
	 * Returns native library manager.
	 *
	 * @return native library manager.
	 */
	public NativeLibraryManager getNativeLibraryManager() {
		return nativeLibraryManager;
	}

	/**
	 * Returns string pool.
	 *
	 * @return string pool.
	 */
	public StringPool getStringPool() {
		return stringPool;
	}

	/**
	 * Returns management interface.
	 *
	 * @return management interface.
	 */
	public ManagementInterface getManagementInterface() {
		return managementInterface;
	}

	/**
	 * Returns time manager.
	 *
	 * @return time manager.
	 */
	public TimeManager getTimeManager() {
		return timeManager;
	}

	/**
	 * Returns class loaders storage.
	 *
	 * @return class loaders storage.
	 */
	public ClassLoaders getClassLoaders() {
		return classLoaders;
	}

	/**
	 * Returns execution engine.
	 *
	 * @return execution engine.
	 */
	public ExecutionEngine getExecutionEngine() {
		return executionEngine;
	}

	/**
	 * Returns safepoint mechanism.
	 *
	 * @return safepoint mechanism.
	 */
	public SafePoint getSafePoint() {
		return safePoint;
	}

	/**
	 * Returns mirror factory.
	 *
	 * @return mirror factory.
	 */
	public MirrorFactory getMirrorFactory() {
		return mirrorFactory;
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
		return currentJavaThread().getOsThread().getStorage();
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
	 * Returns boot class loader data.
	 *
	 * @return boot class loader data.
	 */
	public ClassLoaderData getBootClassLoaderData() {
		return bootClassLoader.getData();
	}

	/**
	 * Searches for bootstrap class.
	 *
	 * @param name       Name of the class.
	 * @param initialize True if class should be initialized if found.
	 * @return bootstrap class or {@code null}, if not found.
	 */
	public JavaClass findBootstrapClass(String name, boolean initialize) {
		JavaClass jc = bootClassLoader.findBootClass(name);
		if (jc != null && initialize) {
			jc.initialize();
		}
		return jc;
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

	/**
	 * Searches for the class in given loader.
	 *
	 * @param loader     Class loader.
	 * @param name       CLass name.
	 * @param initialize Should class be initialized.
	 */
	public JavaClass findClass(ObjectValue loader, String name, boolean initialize) {
		JavaClass jc;
		VMHelper helper = this.helper;
		if (loader.isNull()) {
			jc = findBootstrapClass(name, initialize);
			if (jc == null) {
				helper.throwException(symbols.java_lang_ClassNotFoundException(), name.replace('/', '.'));
			}
		} else {
			ClassLoaderData data = classLoaders.getClassLoaderData(loader);
			jc = data.getClass(name);
			if (jc == null) {
				JavaMethod method = publicLinkResolver.resolveVirtualMethod(loader, "loadClass", "(Ljava/lang/String;Z)Ljava/lang/Class;");
				Locals locals = getThreadStorage().newLocals(method);
				locals.setReference(0, loader);
				locals.setReference(1, helper.newUtf8(name.replace('/', '.')));
				locals.setInt(2, initialize ? 1 : 0);
				jc = ((JavaValue<JavaClass>) helper.invokeReference(method, locals)).getValue();
			} else if (initialize) {
				jc.initialize();
			}
		}
		return jc;
	}

	/**
	 * Creates a boot class loader.
	 * One may override this method.
	 *
	 * @return boot class loader.
	 */
	protected BootClassLoader createBootClassLoader() {
		return RuntimeBootClassLoader.create();
	}

	/**
	 * Creates memory allocator.
	 * One may override this method.
	 *
	 * @return memory allocator.
	 */
	protected MemoryAllocator createMemoryAllocator() {
		return new NavigableMemoryAllocator();
	}

	/**
	 * Creates memory manager.
	 * One may override this method.
	 *
	 * @return memory manager.
	 */
	protected MemoryManager createMemoryManager() {
		return new SimpleMemoryManager(this);
	}

	/**
	 * Creates class definer.
	 * One may override this method.
	 *
	 * @return class definer.
	 */
	protected ClassDefiner createClassDefiner() {
		return new SimpleClassDefiner();
	}

	/**
	 * Creates thread manager.
	 * One may override this method.
	 *
	 * @return thread manager.
	 */
	protected ThreadManager createThreadManager() {
		return new VirtualThreadManager(this);
	}

	/**
	 * Creates file descriptor manager.
	 * One may override this method.
	 *
	 * @return file descriptor manager.
	 */
	protected FileDescriptorManager createFileDescriptorManager() {
		return new SimpleFileDescriptorManager();
	}

	/**
	 * Creates native library manager.
	 * One may override this method.
	 *
	 * @return native library manager.
	 */
	protected NativeLibraryManager createNativeLibraryManager() {
		return new SimpleNativeLibraryManager();
	}

	/**
	 * Creates string pool.
	 * One may override this method.
	 *
	 * @return string pool.
	 */
	protected StringPool createStringPool() {
		return new SimpleStringPool(this);
	}

	/**
	 * Creates management interface.
	 * One may override this method.
	 *
	 * @return management interface.
	 */
	protected ManagementInterface createManagementInterface() {
		return new SimpleManagementInterface();
	}

	/**
	 * Creates time manager.
	 * One may override this method.
	 *
	 * @return time manager.
	 */
	protected TimeManager createTimeManager() {
		return new SimpleTimeManager();
	}

	/**
	 * Creates class loaders storage.
	 * One may override this method.
	 *
	 * @return class loaders.
	 */
	protected ClassLoaders createClassLoaders() {
		return new SimpleClassLoaders(this);
	}

	/**
	 * Creates execution engine.
	 * One may override this method.
	 *
	 * @return execution engine.
	 */
	protected ExecutionEngine createExecutionEngine() {
		return new SimpleExecutionEngine(this);
	}

	/**
	 * Creates safepoint mechanism.
	 * One may override this method.
	 *
	 * @return safepoint mechanism.
	 */
	protected SafePoint createSafePoint() {
		return new NoopSafePoint();
	}

	/**
	 * Creates mirror factory.
	 * One may override this method.
	 *
	 * @return mirror factory.
	 */
	protected MirrorFactory createMirrorFactory() {
		return new SimpleMirrorFactory(this);
	}

	/**
	 * Creates public operations.
	 * One may override this method.
	 *
	 * @return vm operations.
	 */
	protected VMOperations createPublicOperations() {
		return new DefaultVMOperations(this, false);
	}

	/**
	 * Creates trusted operations.
	 * One may override this method.
	 *
	 * @return vm operations.
	 */
	protected VMOperations createTrustedOperations() {
		return new DefaultVMOperations(this, true);
	}

	private void init() {
		ThreadManager threadManager = this.threadManager;
		try {
			ClassLoaders classLoaders = this.classLoaders;
			VMInitializer initializer = this.initializer;
			initializer.initBegin(this);
			initializer.initClassLoaders(this);
			// java/lang/Object & java/lang/Class must be loaded manually,
			// otherwise some MemoryManager implementations will bottleneck.
			InstanceJavaClass klass = internalLink("java/lang/Class");
			InstanceJavaClass object = internalLink("java/lang/Object");
			initializer.bootLink(this, klass, object);
			NativeJava.injectPhase1(this);
			classLoaders.initializeBootClass(object);
			classLoaders.initializeBootClass(klass);
			classLoaders.initializeBootOop(klass, klass);
			classLoaders.initializeBootOop(object, klass);
			object.link();
			klass.link();
			InitializedVMSymbols initializedVMSymbols = new InitializedVMSymbols(this);
			((DelegatingVMSymbols) symbols).setSymbols(initializedVMSymbols);
			symbols = initializedVMSymbols;
			InitializedVMPrimitives initializedVMPrimitives = new InitializedVMPrimitives(this);
			((DelegatingVMPrimitives) primitives).setPrimitives(initializedVMPrimitives);
			primitives = initializedVMPrimitives;
			NativeJava.init(this);
			initializer.nativeInit(this);
			threadManager.attachCurrentThread();
			InstanceJavaClass groupClass = symbols.java_lang_ThreadGroup();
			groupClass.initialize();
			ThreadStorage ts = currentOSThread().getStorage();
			// Initialize system group
			InstanceValue sysGroup = memoryManager.newInstance(groupClass);
			{
				JavaMethod init = publicLinkResolver.resolveSpecialMethod(groupClass, "<init>", "()V");
				Locals locals = ts.newLocals(init);
				locals.setReference(0, sysGroup);
				helper.invoke(init, locals);
			}
			systemThreadGroup = sysGroup;
			// Initialize main group
			InstanceValue mainGroup = memoryManager.newInstance(groupClass);
			{
				JavaMethod init = publicLinkResolver.resolveSpecialMethod(groupClass, "<init>", "(Ljava/lang/ThreadGroup;Ljava/lang/String;)V");
				Locals locals = ts.newLocals(init);
				locals.setReference(0, mainGroup);
				locals.setReference(1, sysGroup);
				locals.setReference(2, helper.newUtf8("main"));
				helper.invoke(init, locals);
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
			VMSymbols symbols = this.symbols;
			ThreadManager threadManager = this.threadManager;
			threadManager.attachCurrentThread();
			symbols.java_lang_ClassLoader().initialize();
			VMHelper helper = this.helper;
			InstanceJavaClass sysClass = symbols.java_lang_System();
			MemoryManager memoryManager = this.memoryManager;
			VMOperations ops = publicOperations;

			// Inject unsafe constants
			// This must be done first, otherwise
			// jdk/internal/misc/Unsafe will cache wrong values
			InstanceJavaClass unsafeConstants = (InstanceJavaClass) findBootstrapClass("jdk/internal/misc/UnsafeConstants", true);
			if (unsafeConstants != null) {
				MemoryAllocator memoryAllocator = this.memoryAllocator;
				unsafeConstants.initialize();
				ops.putInt(unsafeConstants, "ADDRESS_SIZE", memoryAllocator.addressSize());
				ops.putInt(unsafeConstants, "PAGE_SIZE", memoryAllocator.pageSize());
				ops.putBoolean(unsafeConstants, "BIG_ENDIAN", memoryAllocator.getByteOrder() == ByteOrder.BIG_ENDIAN);
			}
			LinkResolver linkResolver = this.publicLinkResolver;
			ThreadStorage ts = getThreadStorage();
			sysClass.initialize();
			{
				InstanceValue threadGroup = mainThreadGroup;
				InstanceValue oop = currentJavaThread().getOop();
				ops.putReference(oop, "group", "Ljava/lang/ThreadGroup;", threadGroup);
				JavaMethod add = linkResolver.resolveVirtualMethod(threadGroup, "add", "(Ljava/lang/Thread;)V");
				Locals locals = ts.newLocals(add);
				locals.setReference(0, threadGroup);
				locals.setReference(1, oop);
				helper.invoke(add, locals);
			}
			findBootstrapClass("java/lang/reflect/Method", true);
			findBootstrapClass("java/lang/reflect/Field", true);
			findBootstrapClass("java/lang/reflect/Constructor", true);

			JavaMethod initializeSystemClass = sysClass.getStaticMethod("initializeSystemClass", "()V");
			if (initializeSystemClass != null) {
				// pre JDK 9 boot
				helper.invoke(initializeSystemClass, ts.newLocals(initializeSystemClass));
			} else {
				findBootstrapClass("java/lang/StringUTF16", true);
				{
					JavaMethod initPhase1 = linkResolver.resolveStaticMethod(sysClass, "initPhase1", "()V");
					helper.invoke(initPhase1, ts.newLocals(initPhase1));
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
					result = helper.invokeInt(initPhase2, locals);
				}
				if (result != 0) {
					throw new IllegalStateException("VM bootstrapping failed, initPhase2 returned " + result);
				}
				{
					JavaMethod initPhase3 = linkResolver.resolveStaticMethod(sysClass, "initPhase3", "()V");
					helper.invoke(initPhase3, ts.newLocals(initPhase3));
				}
			}
			{
				JavaMethod getSystemClassLoader = linkResolver.resolveStaticMethod(symbols.java_lang_ClassLoader(), "getSystemClassLoader", "()Ljava/lang/ClassLoader;");
				helper.invoke(getSystemClassLoader, ts.newLocals(getSystemClassLoader));
			}
			state.set(InitializationState.BOOTED);
		} catch (Exception ex) {
			state.set(InitializationState.FAILED);
			throw new IllegalStateException("VM bootstrap failed", ex);
		}
		//</editor-fold>
	}

	private InstanceJavaClass internalLink(String name) {
		ClassParseResult result = bootClassLoader.lookup(name);
		if (result == null) {
			throw new IllegalStateException("Bootstrap class not found: " + name);
		}
		ClassReader cr = result.getClassReader();
		ClassNode node = result.getNode();
		InstanceJavaClass jc = mirrorFactory.newInstanceClass(memoryManager.nullValue(), cr, node);
		bootClassLoader.forceLink(jc);
		return jc;
	}

	private static final class DummyVMInitializer implements VMInitializer {

		static final VMInitializer INSTANCE = new DummyVMInitializer();
	}
}
