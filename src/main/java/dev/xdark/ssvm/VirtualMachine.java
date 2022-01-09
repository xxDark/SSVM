package dev.xdark.ssvm;

import dev.xdark.ssvm.api.VMCall;
import dev.xdark.ssvm.api.VMInterface;
import dev.xdark.ssvm.classloading.*;
import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.memory.MemoryManager;
import dev.xdark.ssvm.memory.SimpleMemoryManager;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.mirror.JavaClass;
import dev.xdark.ssvm.util.VMHelper;
import dev.xdark.ssvm.util.VMPrimitives;
import dev.xdark.ssvm.util.VMSymbols;
import dev.xdark.ssvm.value.NullValue;
import dev.xdark.ssvm.value.ObjectValue;
import dev.xdark.ssvm.value.Value;

public class VirtualMachine {

	private final BootClassLoaderHolder bootClassLoader;
	private final VMInterface vmInterface;
	private final MemoryManager memoryManager;
	private final VMSymbols symbols;
	private final VMPrimitives primitives;
	private final VMHelper helper;
	private final ClassDefiner classDefiner;

	public VirtualMachine() {
		bootClassLoader = new BootClassLoaderHolder(this, createBootClassLoader());
		// java/lang/Class init
		var bootClassLoader = this.bootClassLoader;
		ClassParseResult lookup;
		try {
			lookup = bootClassLoader.lookup("java/lang/Class");
			if (lookup == null) {
				throw new NullPointerException();
			}
		} catch (Exception ex) {
			throw new IllegalStateException("Unable to locate java/lang/Class", ex);
		}
		var memoryManager = createMemoryManager();
		var jc = new InstanceJavaClass(this, NullValue.INSTANCE, lookup.getClassReader(), lookup.getNode());
		bootClassLoader.forceLink(jc);
		this.memoryManager = memoryManager;
		vmInterface = new VMInterface();
		jc.initialize();
		jc.setOop(memoryManager.newOopForClass(jc));
		symbols = new VMSymbols(this);
		primitives = new VMPrimitives(this);
		helper = new VMHelper(this);
		classDefiner = createClassDefiner();
		NativeJava.vmInit(this);
	}

	/**
	 * Bootstraps virtual machine.
	 */
	public void bootstrap() {
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
	public VMInterface getVmInterface() {
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
			((InstanceJavaClass) jc).initialize();
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
			jc = memoryManager.readClass((ObjectValue) helper.invokeVirtual(symbols.java_lang_ClassLoader, "loadClass", "(Ljava/lang/String;Z)Ljava/lang/Class;", new Value[0], new Value[0]).getResult());
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
		// TODO exception handling.
		var instructions = ctx.getMethod().instructions;
		while (true) {
			var pos = ctx.getInsnPosition();
			ctx.setInsnPosition(pos + 1);
			var insn = instructions.get(pos);
			// TODO handle misc. instructions
			if (insn.getOpcode() == -1) continue;
			var processor = vmi.getProcessor(insn);
			if (processor == null) {
				throw new IllegalStateException("No implemented processor for " + insn.getOpcode());
			}
			var result = processor.execute(insn, ctx);
			if (result == Result.ABORT) break;
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
}
