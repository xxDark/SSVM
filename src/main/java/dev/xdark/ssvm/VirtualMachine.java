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
import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.ExecutionContextOptions;
import dev.xdark.ssvm.execution.ExecutionEngine;
import dev.xdark.ssvm.execution.NoopSafePoint;
import dev.xdark.ssvm.execution.SafePoint;
import dev.xdark.ssvm.execution.SimpleExecutionEngine;
import dev.xdark.ssvm.fs.FileDescriptorManager;
import dev.xdark.ssvm.fs.SimpleFileDescriptorManager;
import dev.xdark.ssvm.jvm.ManagementInterface;
import dev.xdark.ssvm.jvm.SimpleManagementInterface;
import dev.xdark.ssvm.memory.gc.GarbageCollector;
import dev.xdark.ssvm.memory.management.MemoryManager;
import dev.xdark.ssvm.memory.management.SimpleMemoryManager;
import dev.xdark.ssvm.memory.management.SimpleStringPool;
import dev.xdark.ssvm.memory.management.StringPool;
import dev.xdark.ssvm.memory.allocation.MemoryAllocator;
import dev.xdark.ssvm.memory.allocation.SimpleMemoryAllocator;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.mirror.JavaClass;
import dev.xdark.ssvm.mirror.JavaMethod;
import dev.xdark.ssvm.natives.IntrinsicsNatives;
import dev.xdark.ssvm.nt.NativeLibraryManager;
import dev.xdark.ssvm.nt.SimpleNativeLibraryManager;
import dev.xdark.ssvm.symbol.InitializedVMPrimitives;
import dev.xdark.ssvm.symbol.InitializedVMSymbols;
import dev.xdark.ssvm.symbol.UninitializedVMPrimitives;
import dev.xdark.ssvm.symbol.UninitializedVMSymbols;
import dev.xdark.ssvm.symbol.VMPrimitives;
import dev.xdark.ssvm.symbol.VMSymbols;
import dev.xdark.ssvm.thread.NopThreadManager;
import dev.xdark.ssvm.thread.ThreadManager;
import dev.xdark.ssvm.thread.ThreadStorage;
import dev.xdark.ssvm.thread.VMThread;
import dev.xdark.ssvm.tz.SimpleTimeManager;
import dev.xdark.ssvm.tz.TimeManager;
import dev.xdark.ssvm.util.InvokeDynamicLinker;
import dev.xdark.ssvm.util.VMHelper;
import dev.xdark.ssvm.util.VMOperations;
import dev.xdark.ssvm.value.InstanceValue;
import dev.xdark.ssvm.value.IntValue;
import dev.xdark.ssvm.value.JavaValue;
import dev.xdark.ssvm.value.ObjectValue;
import dev.xdark.ssvm.value.Value;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicReference;

public class VirtualMachine {

	private static final ExecutionContextOptions USE_INVOKERS = ExecutionContextOptions.builder().build();
	private static final ExecutionContextOptions NO_INVOKERS = ExecutionContextOptions.builder()
		.useInvokers(false)
		.build();
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
	private final Properties properties;
	private final Map<String, String> env;
	private final VMInitializer initializer;
	private final ExecutionEngine executionEngine;
	private final SafePoint safePoint;
	private final VMOperations operations;
	private final LinkResolver linkResolver;
	private final InvokeDynamicLinker invokeDynamicLinker;
	private volatile InstanceValue systemThreadGroup;
	private volatile InstanceValue mainThreadGroup;

	public VirtualMachine(VMInitializer initializer) {
		this.initializer = initializer;
		ClassLoaders classLoaders = createClassLoaders();
		this.classLoaders = classLoaders;
		memoryAllocator = createMemoryAllocator();
		safePoint = createSafePoint();
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
		linkResolver = new LinkResolver(this);
		invokeDynamicLinker = new InvokeDynamicLinker(this);
		operations = new VMOperations(this);

		(properties = new Properties()).putAll(System.getProperties());
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
	 * Attempts to initialize the VM.
	 */
	private void tryInitialize() {
		if (state.compareAndSet(InitializationState.UNINITIALIZED, InitializationState.INITIALIZING)) {
			init();
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
	 *
	 * @throws IllegalStateException If VM fails to transit to {@link InitializationState#BOOTING} state,
	 *                               or fails to boot.
	 */
	public void bootstrap() {
		//<editor-fold desc="VM bootstrap">
		tryInitialize();
		assertInitialized();
		if (state.compareAndSet(InitializationState.INITIALIZED, InitializationState.BOOTING)) {
			try {
				VMSymbols symbols = this.symbols;
				symbols.java_lang_ClassLoader().initialize();
				VMHelper helper = this.helper;
				InstanceJavaClass sysClass = symbols.java_lang_System();
				MemoryManager memoryManager = this.memoryManager;
				ThreadManager threadManager = this.threadManager;
				GarbageCollector garbageCollector = memoryManager.getGarbageCollector();

				// Inject unsafe constants
				// This must be done first, otherwise
				// jdk/internal/misc/Unsafe will cache wrong values
				InstanceJavaClass unsafeConstants = (InstanceJavaClass) findBootstrapClass("jdk/internal/misc/UnsafeConstants", true);
				if (unsafeConstants != null) {
					MemoryAllocator memoryAllocator = this.memoryAllocator;
					unsafeConstants.initialize();
					unsafeConstants.setStaticFieldValue("ADDRESS_SIZE0", "I", IntValue.of(memoryAllocator.addressSize()));
					unsafeConstants.setStaticFieldValue("PAGE_SIZE", "I", IntValue.of(memoryAllocator.pageSize()));
					unsafeConstants.setStaticFieldValue("BIG_ENDIAN", "Z", memoryAllocator.getByteOrder() == ByteOrder.BIG_ENDIAN ? IntValue.ONE : IntValue.ZERO);
				}
				// Initialize system group
				InstanceJavaClass groupClass = symbols.java_lang_ThreadGroup();
				InstanceValue sysGroup = memoryManager.newInstance(groupClass);
				garbageCollector.makeGlobalReference(sysGroup);
				helper.invokeExact(groupClass, "<init>", "()V", new Value[0], new Value[]{sysGroup});
				systemThreadGroup = sysGroup;
				// Initialize main group
				InstanceValue mainGroup = memoryManager.newInstance(groupClass);
				garbageCollector.makeGlobalReference(mainGroup);
				helper.invokeExact(groupClass, "<init>", "(Ljava/lang/ThreadGroup;Ljava/lang/String;)V", new Value[0],
					new Value[]{mainGroup, sysGroup, helper.newUtf8("main")});
				mainThreadGroup = mainGroup;
				// Initialize main thread
				VMThread mainThread = threadManager.createMainThread();
				InstanceValue oop = mainThread.getOop();
				oop.setValue("group", "Ljava/lang/ThreadGroup;", mainGroup);
				oop.setValue("name", "Ljava/lang/String;", helper.newUtf8("main"));
				sysClass.initialize();
				findBootstrapClass("java/lang/reflect/Method", true);
				findBootstrapClass("java/lang/reflect/Field", true);
				findBootstrapClass("java/lang/reflect/Constructor", true);

				JavaMethod initializeSystemClass = sysClass.getStaticMethod("initializeSystemClass", "()V");
				if (initializeSystemClass != null) {
					// pre JDK 9 boot
					helper.invokeStatic(initializeSystemClass, new Value[0], new Value[0]);
				} else {
					findBootstrapClass("java/lang/StringUTF16", true);

					// Oracle had moved this to native code, do it here
					// On JDK 8 this is invoked in initializeSystemClass
					helper.invokeVirtual("add", "(Ljava/lang/Thread;)V", new Value[0], new Value[]{
						mainGroup,
						mainThread.getOop()
					});
					helper.invokeStatic(sysClass, "initPhase1", "()V", new Value[0], new Value[0]);
					findBootstrapClass("java/lang/invoke/MethodHandle", true);
					findBootstrapClass("java/lang/invoke/ResolvedMethodName", true);
					findBootstrapClass("java/lang/invoke/MemberName", true);
					findBootstrapClass("java/lang/invoke/MethodHandleNatives", true);

					int result = helper.invokeStatic(sysClass, "initPhase2", "(ZZ)I", new Value[0], new Value[]{IntValue.ONE, IntValue.ONE}).getResult().asInt();
					if (result != 0) {
						throw new IllegalStateException("VM bootstrapping failed, initPhase2 returned " + result);
					}
					helper.invokeStatic(sysClass, "initPhase3", "()V", new Value[0], new Value[0]);
				}
				helper.invokeStatic(symbols.java_lang_ClassLoader(), "getSystemClassLoader", "()Ljava/lang/ClassLoader;", new Value[0], new Value[0]);
				state.set(InitializationState.BOOTED);
			} catch (Exception ex) {
				state.set(InitializationState.FAILED);
				throw new IllegalStateException("VM bootstrap failed", ex);
			}
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
	public Properties getProperties() {
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
	public VMOperations getOperations() {
		return operations;
	}

	/**
	 * Returns link resolver.
	 *
	 * @return link resolver.
	 */
	public LinkResolver getLinkResolver() {
		return linkResolver;
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
		return currentThread().getThreadStorage();
	}

	/**
	 * Returns current VM thread.
	 *
	 * @return current VM thread.
	 */
	public VMThread currentThread() {
		return threadManager.currentThread();
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
				jc = ((JavaValue<JavaClass>) helper.invokeVirtual("loadClass", "(Ljava/lang/String;Z)Ljava/lang/Class;", new Value[0], new Value[]{loader, helper.newUtf8(name.replace('/', '.')), initialize ? IntValue.ONE : IntValue.ZERO}).getResult()).getValue();
			} else if (initialize) {
				jc.initialize();
			}
		}
		return jc;
	}

	/**
	 * Processes {@link ExecutionContext}.
	 *
	 * @param ctx     Context to process.
	 * @param options Execution options.
	 */
	public void execute(ExecutionContext ctx, ExecutionContextOptions options) {
		executionEngine.execute(ctx, options);
	}

	/**
	 * Processes {@link ExecutionContext}.
	 *
	 * @param ctx Context to process.
	 */
	public void execute(ExecutionContext ctx) {
		ExecutionEngine executionEngine = this.executionEngine;
		executionEngine.execute(ctx, executionEngine.defaultOptions());
	}

	/**
	 * Processes {@link ExecutionContext}.
	 *
	 * @param ctx         Context to process.
	 * @param useInvokers Should VM search for VMI hooks.
	 * @deprecated Use {@link VirtualMachine#execute(ExecutionContext, ExecutionContextOptions)} instead.
	 */
	@Deprecated
	public void execute(ExecutionContext ctx, boolean useInvokers) {
		executionEngine.execute(ctx, useInvokers ? USE_INVOKERS : NO_INVOKERS);
	}

	/**
	 * Causes the tasks of current thread to execute.
	 *
	 * @see VMThread#getTaskQueue()
	 */
	public void drainTaskQueue() {
		Queue<Runnable> queue = currentThread().getTaskQueue();
		Runnable r;
		while ((r = queue.poll()) != null) {
			r.run();
		}
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
		return new SimpleMemoryAllocator();
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
		return new NopThreadManager(this);
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

	private void init() {
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
			InstanceJavaClass groupClass = symbols.java_lang_ThreadGroup();
			groupClass.initialize();
			IntrinsicsNatives.init(this);
			state.set(InitializationState.INITIALIZED);
		} catch (Exception ex) {
			state.set(InitializationState.FAILED);
			throw new IllegalStateException("VM initialization failed", ex);
		}
	}

	private InstanceJavaClass internalLink(String name) {
		ClassParseResult result = bootClassLoader.lookup(name);
		if (result == null) {
			throw new IllegalStateException("Bootstrap class not found: " + name);
		}
		ClassReader cr = result.getClassReader();
		ClassNode node = result.getNode();
		InstanceJavaClass jc = classLoaders.constructClass(memoryManager.nullValue(), cr, node);
		bootClassLoader.forceLink(jc);
		return jc;
	}

	private static final class DummyVMInitializer implements VMInitializer {

		static final VMInitializer INSTANCE = new DummyVMInitializer();
	}
}
