package dev.xdark.ssvm;

import dev.xdark.ssvm.api.VMInterface;
import dev.xdark.ssvm.classloading.BootClassLoader;
import dev.xdark.ssvm.classloading.RuntimeBootClassLoader;
import dev.xdark.ssvm.memory.MemoryManager;
import dev.xdark.ssvm.memory.SimpleMemoryManager;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.mirror.JavaClass;
import dev.xdark.ssvm.util.VMHelper;
import dev.xdark.ssvm.util.VMSymbols;
import dev.xdark.ssvm.value.NullValue;

public class VirtualMachine {

	private final BootClassLoaderHolder bootClassLoader;
	private final VMInterface vmInterface;
	private final MemoryManager memoryManager;
	private final VMSymbols symbols;
	private final VMHelper helper;

	public VirtualMachine() {
		bootClassLoader = new BootClassLoaderHolder(this, createBootClassLoader());
		// java/lang/Class init
		var bootClassLoader = this.bootClassLoader;
		BootClassLoader.LookupResult lookup;
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
		symbols = createSymbols();
		helper = new VMHelper(this);
	}

	/**
	 * Bootstraps virtual machine.
	 *
	 * @throws Exception
	 * 		If any error occurs.
	 */
	public void bootstrap() throws Exception {
		NativeJava.vmInit(this);
		findBootstrapClass("java/lang/Class");
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
	 * Returns VM helper.
	 *
	 * @return VM helper.
	 */
	public VMHelper getHelper() {
		return helper;
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
	 *
	 * @throws Exception
	 * 		if any error occurs.
	 */
	public JavaClass findBootstrapClass(String name, boolean initialize) throws Exception {
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
	 *
	 * @throws Exception
	 * 		if any error occurs.
	 */
	public JavaClass findBootstrapClass(String name) throws Exception {
		return findBootstrapClass(name, false);
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

	private VMSymbols createSymbols() {
		try {
			return new VMSymbols(this);
		} catch (Exception ex) {
			throw new IllegalStateException("Could not create VM symbols", ex);
		}
	}
}
