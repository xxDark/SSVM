package dev.xdark.ssvm;

import dev.xdark.ssvm.api.VMCall;
import dev.xdark.ssvm.api.VMInterface;
import dev.xdark.ssvm.classloading.*;
import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.execution.VMException;
import dev.xdark.ssvm.fs.FileDescriptorManager;
import dev.xdark.ssvm.fs.SimpleFileDescriptorManager;
import dev.xdark.ssvm.memory.MemoryManager;
import dev.xdark.ssvm.memory.SimpleMemoryManager;
import dev.xdark.ssvm.mirror.ClassLayout;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.mirror.JavaClass;
import dev.xdark.ssvm.thread.NopThreadManager;
import dev.xdark.ssvm.thread.ThreadManager;
import dev.xdark.ssvm.thread.VMThread;
import dev.xdark.ssvm.util.AsmUtil;
import dev.xdark.ssvm.util.VMHelper;
import dev.xdark.ssvm.util.VMPrimitives;
import dev.xdark.ssvm.util.VMSymbols;
import dev.xdark.ssvm.value.IntValue;
import dev.xdark.ssvm.value.NullValue;
import dev.xdark.ssvm.value.ObjectValue;
import dev.xdark.ssvm.value.Value;
import org.objectweb.asm.Opcodes;

import java.nio.ByteOrder;
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
	private final Properties properties;

	public VirtualMachine() {
		bootClassLoader = new BootClassLoaderHolder(this, createBootClassLoader());
		// java/lang/Object & java/lang/Class must be loaded manually,
		// otherwise some MemoryManager implementations will bottleneck.
		var klass = internalLink("java/lang/Class");
		var object = internalLink("java/lang/Object");
		memoryManager = createMemoryManager();
		vmInterface = new VMInterface();
		helper = new VMHelper(this);
		threadManager = createThreadManager();
		object.setVirtualLayout(ClassLayout.EMPTY);
		object.setStaticLayout(ClassLayout.EMPTY);
		klass.setVirtualLayout(klass.createVirtualLayout());
		klass.setStaticLayout(klass.createStaticLayout());
		klass.buildVirtualFields();
		setClassOop(klass, klass);
		setClassOop(object, klass);
		symbols = new VMSymbols(this);
		primitives = new VMPrimitives(this);
		classDefiner = createClassDefiner();
		fileDescriptorManager = createFileDescriptorManager();
		NativeJava.vmInit(this);

		object.initialize();
		klass.initialize();
		(properties = new Properties()).putAll(System.getProperties());
	}

	/**
	 * Full VM initialization.
	 */
	public void bootstrap() {
		var symbols = this.symbols;
		symbols.java_lang_ClassLoader.initialize();
		var helper = this.helper;
		var sysClass = symbols.java_lang_System;
		var memoryManager = this.memoryManager;
		var threadManager = this.threadManager;

		var groupClass = symbols.java_lang_ThreadGroup;
		groupClass.initialize();
		// Initialize system group
		var sysGroup = memoryManager.newInstance(groupClass);
		helper.invokeExact(groupClass, "<init>", "()V", new Value[0], new Value[]{sysGroup});
		// Initialize main thread
		var mainThread = threadManager.getVmThread(Thread.currentThread());
		var oop = mainThread.getOop();
		oop.setValue("group", "Ljava/lang/ThreadGroup;", sysGroup);
		helper.invokeExact(groupClass, "add", "(Ljava/lang/Thread;)V", new Value[0], new Value[]{sysGroup, oop});

		sysClass.initialize();
		var initializeSystemClass = sysClass.getMethod("initializeSystemClass", "()V");
		if (initializeSystemClass != null) {
			// pre JDK 9 boot
			helper.invokeStatic(sysClass, initializeSystemClass, new Value[0], new Value[0]);
		} else {
			var unsafeConstants = (InstanceJavaClass) findBootstrapClass("jdk/internal/misc/UnsafeConstants", true);
			if (unsafeConstants != null) {
				// Inject constants
				unsafeConstants.initialize();
				unsafeConstants.setFieldValue("ADDRESS_SIZE0", "I", new IntValue(memoryManager.addressSize()));
				unsafeConstants.setFieldValue("PAGE_SIZE", "I", new IntValue(memoryManager.pageSize()));
				unsafeConstants.setFieldValue("BIG_ENDIAN", "Z", new IntValue(memoryManager.getByteOrder() == ByteOrder.BIG_ENDIAN ? 1 : 0));
			}
			findBootstrapClass("java/lang/StringUTF16", true);
			helper.invokeStatic(sysClass, "initPhase1", "()V", new Value[0], new Value[0]);
			var result = helper.invokeStatic(sysClass, "initPhase2", "(ZZ)I", new Value[0], new Value[]{new IntValue(1), new IntValue(1)}).getResult().asInt();
			if (result != 0) {
				throw new IllegalStateException("VM initialization failed, initPhase2 returned " + result);
			}
			helper.invokeStatic(sysClass, "initPhase3", "()V", new Value[0], new Value[0]);
		}
		var classLoaderClass = symbols.java_lang_ClassLoader;
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
	 * Returns current VM thread.
	 *
	 * @return current VM thread.
	 */
	public VMThread currentThread() {
		return threadManager.getVmThread(Thread.currentThread());
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
		var jc = bootClassLoader.findBootClass(name);
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
		if (loader.isNull()) {
			jc = findBootstrapClass(name, initialize);
		} else {
			var helper = this.helper;
			jc = memoryManager.readClass((ObjectValue) helper.invokeVirtual("loadClass", "(Ljava/lang/String;Z)Ljava/lang/Class;", new Value[0], new Value[]{loader, helper.newUtf8(name), new IntValue(initialize ? 1 : 0)}).getResult());
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
		var backtrace = currentThread().getBacktrace();
		backtrace.push(ctx);
		var mn = ctx.getMethod();
		try {
			var vmi = vmInterface;
			if (useInvokers) {
				var invoker = vmi.getInvoker(new VMCall(ctx.getOwner(), ctx.getMethod()));
				if (invoker != null) {
					var result = invoker.intercept(ctx);
					if (result == Result.ABORT) {
						return;
					}
				}
			}
			if ((mn.access & Opcodes.ACC_NATIVE) != 0) {
				helper.throwException(symbols.java_lang_UnsatisfiedLinkError, ctx.getOwner().getInternalName() + '.' + mn.name + mn.desc);
			}
			var instructions = mn.instructions;
			exec:
			while (true) {
				try {
					var pos = ctx.getInsnPosition();
					ctx.setInsnPosition(pos + 1);
					var insn = instructions.get(pos);
					// TODO handle misc. instructions
					if (insn.getOpcode() == -1) continue;
					var processor = vmi.getProcessor(insn);
					if (processor == null) {
						helper.throwException(symbols.java_lang_InternalError, "No implemented processor for " + insn.getOpcode());
						continue;
					}
					var result = processor.execute(insn, ctx);
					if (result == Result.ABORT) break;
				} catch (VMException ex) {
					var oop = ex.getOop();
					var tryCatchBlocks = mn.tryCatchBlocks;
					var index = ctx.getInsnPosition() - 1;
					for (int i = 0, j = tryCatchBlocks.size(); i < j; i++) {
						var block = tryCatchBlocks.get(i);
						var type = block.type;
						if (type == null) continue;
						if (index < AsmUtil.getIndex(block.start) || index > AsmUtil.getIndex(block.end)) continue;
						var stack = ctx.getStack();
						stack.clear();
						stack.push(oop);
						ctx.setInsnPosition(AsmUtil.getIndex(block.handler));
						continue exec;
					}
					throw ex;
				}
			}
		} catch (VMException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new IllegalStateException("Uncaught VM error at: " + ctx.getOwner().getInternalName() + '.' + mn.name + mn.desc, ex);
		} finally {
			backtrace.pop();
		}
	}

	/**
	 * Creates a boot class loader.
	 * One may override this method.
	 *
	 * @return boot class loader.
	 */
	protected BootClassLoader createBootClassLoader() {
		return new RuntimeBootClassLoader();
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

	private InstanceJavaClass internalLink(String name) {
		var result = bootClassLoader.lookup(name);
		if (result == null) {
			throw new IllegalStateException("Bootstrap class not found: " + name);
		}
		var cr = result.getClassReader();
		var node = result.getNode();
		var jc = new InstanceJavaClass(this, NullValue.INSTANCE, cr, node);
		bootClassLoader.forceLink(jc);
		return jc;
	}

	private void setClassOop(InstanceJavaClass javaClass, InstanceJavaClass jlc) {
		var oop = jlc == javaClass ? memoryManager.newJavaLangClass(javaClass, javaClass) : memoryManager.setOopForClass(javaClass);
		javaClass.setOop(oop);
		helper.initializeDefaultValues(oop);
	}
}
