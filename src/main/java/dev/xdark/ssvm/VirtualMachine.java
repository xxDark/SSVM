package dev.xdark.ssvm;

import dev.xdark.ssvm.api.VMInterface;
import dev.xdark.ssvm.classloading.BootClassLoader;
import dev.xdark.ssvm.classloading.ClassDefiner;
import dev.xdark.ssvm.classloading.ClassLoaderData;
import dev.xdark.ssvm.classloading.RuntimeBootClassLoader;
import dev.xdark.ssvm.classloading.SimpleClassDefiner;
import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.Interpreter;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.execution.VMException;
import dev.xdark.ssvm.fs.FileDescriptorManager;
import dev.xdark.ssvm.fs.SimpleFileDescriptorManager;
import dev.xdark.ssvm.jvm.ManagementInterface;
import dev.xdark.ssvm.jvm.SimpleManagementInterface;
import dev.xdark.ssvm.memory.MemoryManager;
import dev.xdark.ssvm.memory.SimpleMemoryManager;
import dev.xdark.ssvm.memory.SimpleStringPool;
import dev.xdark.ssvm.memory.StringPool;
import dev.xdark.ssvm.mirror.FieldLayout;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.mirror.JavaClass;
import dev.xdark.ssvm.natives.IntrinsicsNatives;
import dev.xdark.ssvm.nt.NativeLibraryManager;
import dev.xdark.ssvm.nt.SimpleNativeLibraryManager;
import dev.xdark.ssvm.thread.NopThreadManager;
import dev.xdark.ssvm.thread.StackFrame;
import dev.xdark.ssvm.thread.ThreadManager;
import dev.xdark.ssvm.thread.ThreadStorage;
import dev.xdark.ssvm.thread.VMThread;
import dev.xdark.ssvm.tz.SimpleTimeZoneManager;
import dev.xdark.ssvm.tz.TimeZoneManager;
import dev.xdark.ssvm.util.VMHelper;
import dev.xdark.ssvm.util.VMPrimitives;
import dev.xdark.ssvm.util.VMSymbols;
import dev.xdark.ssvm.value.InstanceValue;
import dev.xdark.ssvm.value.IntValue;
import dev.xdark.ssvm.value.JavaValue;
import dev.xdark.ssvm.value.NullValue;
import dev.xdark.ssvm.value.ObjectValue;
import dev.xdark.ssvm.value.Value;
import lombok.val;
import org.objectweb.asm.Opcodes;

import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class VirtualMachine {

	private final BootClassLoaderHolder bootClassLoader;
	private final VMInterface vmInterface;
	private final MemoryManager memoryManager;
	private final VMSymbols symbols;
	private final VMPrimitives primitives;
	private final VMHelper helper;
	private final ClassDefiner classDefiner;
	private final ThreadManager threadManager;
	private final FileDescriptorManager fileDescriptorManager;
	private final NativeLibraryManager nativeLibraryManager;
	private final StringPool stringPool;
	private final ManagementInterface managementInterface;
	private final TimeZoneManager timeZoneManager;
	private final Properties properties;
	private final Map<String, String> env;

	public VirtualMachine() {
		bootClassLoader = new BootClassLoaderHolder(this, createBootClassLoader());
		// java/lang/Object & java/lang/Class must be loaded manually,
		// otherwise some MemoryManager implementations will bottleneck.
		val klass = internalLink("java/lang/Class");
		val object = internalLink("java/lang/Object");
		NativeJava.injectPhase1(this);
		memoryManager = createMemoryManager();
		vmInterface = new VMInterface();
		helper = new VMHelper(this);
		threadManager = createThreadManager();
		object.setVirtualFieldLayout(FieldLayout.EMPTY);
		object.setStaticFieldLayout(FieldLayout.EMPTY);
		klass.setVirtualFieldLayout(klass.createVirtualFieldLayout());
		klass.setStaticFieldLayout(klass.createStaticFieldLayout());
		setClassOop(klass, klass);
		setClassOop(object, klass);
		classDefiner = createClassDefiner();
		val symbols = new VMSymbols(this);
		this.symbols = symbols;
		primitives = new VMPrimitives(this);
		fileDescriptorManager = createFileDescriptorManager();
		nativeLibraryManager = createNativeLibraryManager();
		stringPool = createStringPool();
		managementInterface = createManagementInterface();
		timeZoneManager = createTimeZoneManager();
		NativeJava.init(this);

		(properties = new Properties()).putAll(System.getProperties());
		env = new HashMap<>(System.getenv());
		val groupClass = symbols.java_lang_ThreadGroup;
		groupClass.initialize();

		IntrinsicsNatives.init(this);
	}

	/**
	 * Full VM initialization.
	 */
	public void bootstrap() {
		val symbols = this.symbols;
		symbols.java_lang_ClassLoader.initialize();
		val helper = this.helper;
		val sysClass = symbols.java_lang_System;
		val memoryManager = this.memoryManager;
		val threadManager = this.threadManager;

		// Initialize system group
		val groupClass = symbols.java_lang_ThreadGroup;
		val sysGroup = memoryManager.newInstance(groupClass);
		helper.invokeExact(groupClass, "<init>", "()V", new Value[0], new Value[]{sysGroup});
		// Initialize main thread
		val mainThread = threadManager.currentThread();
		val oop = mainThread.getOop();
		oop.setValue("group", "Ljava/lang/ThreadGroup;", sysGroup);
		helper.invokeExact(groupClass, "add", "(Ljava/lang/Thread;)V", new Value[0], new Value[]{sysGroup, oop});
		sysClass.initialize();
		findBootstrapClass("java/lang/reflect/Method", true);
		findBootstrapClass("java/lang/reflect/Field", true);
		findBootstrapClass("java/lang/reflect/Constructor", true);

		val initializeSystemClass = sysClass.getStaticMethod("initializeSystemClass", "()V");
		if (initializeSystemClass != null) {
			// pre JDK 9 boot
			helper.invokeStatic(sysClass, initializeSystemClass, new Value[0], new Value[0]);
		} else {
			val unsafeConstants = (InstanceJavaClass) findBootstrapClass("jdk/internal/misc/UnsafeConstants", true);
			if (unsafeConstants != null) {
				// Inject constants
				unsafeConstants.initialize();
				unsafeConstants.setFieldValue("ADDRESS_SIZE0", "I", IntValue.of(memoryManager.addressSize()));
				unsafeConstants.setFieldValue("PAGE_SIZE", "I", IntValue.of(memoryManager.pageSize()));
				unsafeConstants.setFieldValue("BIG_ENDIAN", "Z", memoryManager.getByteOrder() == ByteOrder.BIG_ENDIAN ? IntValue.ONE : IntValue.ZERO);
			}
			findBootstrapClass("java/lang/StringUTF16", true);

			helper.invokeStatic(sysClass, "initPhase1", "()V", new Value[0], new Value[0]);
			findBootstrapClass("java/lang/invoke/MethodHandle", true);
			findBootstrapClass("java/lang/invoke/ResolvedMethodName", true);
			findBootstrapClass("java/lang/invoke/MemberName", true);
			findBootstrapClass("java/lang/invoke/MethodHandleNatives", true);

			val result = helper.invokeStatic(sysClass, "initPhase2", "(ZZ)I", new Value[0], new Value[]{IntValue.ONE, IntValue.ONE}).getResult().asInt();
			if (result != 0) {
				throw new IllegalStateException("VM initialization failed, initPhase2 returned " + result);
			}
			helper.invokeStatic(sysClass, "initPhase3", "()V", new Value[0], new Value[0]);
		}
		val classLoaderClass = symbols.java_lang_ClassLoader;
		classLoaderClass.initialize();
		helper.invokeStatic(classLoaderClass, "getSystemClassLoader", "()Ljava/lang/ClassLoader;", new Value[0], new Value[0]);
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
	 * Returns time zone manager.
	 *
	 * @return time zone manager.
	 */
	public TimeZoneManager getTimeZoneManager() {
		return timeZoneManager;
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
	 * @param name
	 * 		Name of the class.
	 * @param initialize
	 * 		True if class should be initialized if found.
	 *
	 * @return bootstrap class or {@code null}, if not found.
	 */
	public JavaClass findBootstrapClass(String name, boolean initialize) {
		val jc = bootClassLoader.findBootClass(name);
		if (jc != null && initialize) {
			jc.initialize();
		}
		return jc;
	}

	/**
	 * Searches for bootstrap class.
	 *
	 * @param name
	 * 		Name of the class.
	 *
	 * @return bootstrap class or {@code null}, if not found.
	 */
	public JavaClass findBootstrapClass(String name) {
		return findBootstrapClass(name, false);
	}

	/**
	 * Searches for the class in given loader.
	 *
	 * @param loader
	 * 		Class loader.
	 * @param name
	 * 		CLass name.
	 * @param initialize
	 * 		Should class be initialized.
	 */
	public JavaClass findClass(Value loader, String name, boolean initialize) {
		JavaClass jc;
		val helper = this.helper;
		if (loader.isNull()) {
			jc = findBootstrapClass(name, initialize);
			if (jc == null) {
				helper.throwException(symbols.java_lang_ClassNotFoundException, name);
			}
		} else {
			val oop = ((JavaValue<ClassLoaderData>) ((InstanceValue) loader).getValue(NativeJava.CLASS_LOADER_OOP, "Ljava/lang/Object;")).getValue();
			jc = oop.getClass(name);
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
	 * @param ctx
	 * 		Context to process.
	 * @param useInvokers
	 * 		Should VM search for VMI hooks.
	 */
	public void execute(ExecutionContext ctx, boolean useInvokers) {
		val jm = ctx.getMethod();
		val access = jm.getAccess();
		val isNative = (access & Opcodes.ACC_NATIVE) != 0;
		if (isNative) {
			ctx.setLineNumber(-2);
		}
		val threadManager = this.threadManager;
		val backtrace = threadManager.currentThread().getBacktrace();
		backtrace.push(StackFrame.ofContext(ctx));
		val vmi = vmInterface;
		jm.increaseInvocation();
		ObjectValue lock = null;
		if ((access & Opcodes.ACC_SYNCHRONIZED) != 0) {
			if (((access & Opcodes.ACC_STATIC)) == 0) {
				lock = ctx.getLocals().load(0);
			} else {
				lock = jm.getOwner().getOop();
			}
			lock.monitorEnter();
		}
		try {
			vmi.getInvocationHooks(jm, true)
					.forEach(invocation -> invocation.handle(ctx));
			if (useInvokers) {
				val invoker = vmi.getInvoker(jm);
				if (invoker != null) {
					val result = invoker.intercept(ctx);
					if (result == Result.ABORT) {
						return;
					}
				}
			}
			if (isNative) {
				helper.throwException(symbols.java_lang_UnsatisfiedLinkError, ctx.getOwner().getInternalName() + '.' + jm.getName() + jm.getDesc());
			}
			if ((access & Opcodes.ACC_ABSTRACT) != 0) {
				helper.throwException(symbols.java_lang_AbstractMethodError, ctx.getOwner().getInternalName() + '.' + jm.getName() + jm.getDesc());
			}
			Interpreter.execute(ctx);
		} catch (VMException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new IllegalStateException("Uncaught VM error at: " + ctx.getOwner().getInternalName() + '.' + jm.getName() + jm.getDesc(), ex);
		} finally {
			try {
				try {
					vmi.getInvocationHooks(jm, false)
						.forEach(invocation -> invocation.handle(ctx));
				} finally {
					ctx.deallocate();
					if (lock != null) {
						lock.monitorExit();
					}
				}
			} finally {
				backtrace.pop();
			}
		}
	}

	/**
	 * Causes the tasks of current thread to execute.
	 *
	 * @see VMThread#getTaskQueue()
	 */
	public void drainTaskQueue() {
		val queue = currentThread().getTaskQueue();
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
	 * Creates time zone manager.
	 * One may override this method.
	 *
	 * @return time zone manager.
	 */
	protected TimeZoneManager createTimeZoneManager() {
		return new SimpleTimeZoneManager();
	}

	private InstanceJavaClass internalLink(String name) {
		val result = bootClassLoader.lookup(name);
		if (result == null) {
			throw new IllegalStateException("Bootstrap class not found: " + name);
		}
		val cr = result.getClassReader();
		val node = result.getNode();
		val jc = new InstanceJavaClass(this, NullValue.INSTANCE, cr, node);
		bootClassLoader.forceLink(jc);
		return jc;
	}

	private void setClassOop(InstanceJavaClass javaClass, InstanceJavaClass jlc) {
		JavaValue<InstanceJavaClass> oop = jlc == javaClass ? memoryManager.newJavaLangClass(javaClass) : memoryManager.setOopForClass(javaClass);
		javaClass.setOop(oop);
		helper.initializeDefaultValues(oop);
	}
}
