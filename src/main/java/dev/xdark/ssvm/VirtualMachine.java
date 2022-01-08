package dev.xdark.ssvm;

import dev.xdark.ssvm.api.VMInterface;
import dev.xdark.ssvm.classloading.BootClassLoader;
import dev.xdark.ssvm.classloading.RuntimeBootClassLoader;
import dev.xdark.ssvm.memory.MemoryManager;
import dev.xdark.ssvm.mirror.JavaClass;

public class VirtualMachine {

	private final BootClassLoaderHolder bootClassLoader;
	private final VMInterface vmInterface;
	private final MemoryManager memoryManager;

	public VirtualMachine() {
		bootClassLoader = new BootClassLoaderHolder(this, createBootClassLoader());
		memoryManager = createMemoryManager();
		vmInterface = new VMInterface();
	}

	public void bootstrap() throws Exception {
		// java/lang/Class init
		var bootClassLoader = this.bootClassLoader;
		var lookup = bootClassLoader.lookup("java/lang/Class");
		if (lookup == null) {
			throw new IllegalStateException("Unable to locate java/lang/Class");
		}
		NativeJava.vmInit(this);
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
		return bootClassLoader.findBootClass(name);
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
		// TODO
		throw new UnsupportedOperationException();
	}
}
