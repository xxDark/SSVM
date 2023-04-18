package dev.xdark.ssvm;

import dev.xdark.ssvm.classloading.BootClassLoader;
import dev.xdark.ssvm.classloading.ClassLoaderData;
import dev.xdark.ssvm.classloading.ClassLoaders;
import dev.xdark.ssvm.classloading.ClassParseResult;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.mirror.JavaClass;

/**
 * Holder for the boot class loader.
 *
 * @author xDark
 */
final class BootClassLoaderHolder {

	private final ClassLoaderData data;
	private final VirtualMachine vm;
	private final BootClassLoader bootClassLoader;

	/**
	 * Boot class loader to get classes from.
	 *
	 * @param vm              VM instance.
	 * @param bootClassLoader Boot class loader.
	 * @param data            Class storage.
	 */
	BootClassLoaderHolder(VirtualMachine vm, BootClassLoader bootClassLoader, ClassLoaderData data) {
		this.vm = vm;
		this.bootClassLoader = bootClassLoader;
		this.data = data;
	}

	/**
	 * Resolves boot class by it's name.
	 *
	 * @param name Name of the class.
	 * @return Resolved class or {@code null}, if not found.
	 */
	JavaClass findBootClass(String name) {
		int dimensions = 0;
		while (name.charAt(dimensions) == '[') {
			dimensions++;
		}
		ClassLoaderData data = this.data;
		String trueName = dimensions == 0 ? name : name.substring(dimensions + 1, name.length() - 1);
		InstanceJavaClass jc;
		JavaClass res;
		synchronized (data) {
			jc = data.getClass(trueName);
			if (jc == null) {
				ClassParseResult result = bootClassLoader.findBootClass(trueName);
				if (result == null) {
					return null;
				}
				VirtualMachine vm = this.vm;
				ClassLoaders classLoaders = vm.getClassLoaders();
				jc = vm.getMirrorFactory().newInstanceClass(vm.getMemoryManager().nullValue(), result.getClassReader(), result.getNode());
				classLoaders.setClassOop(jc);
				data.linkClass(jc);
			}
			res = jc;
		}
		while (dimensions-- != 0) {
			res = res.newArrayClass();
		}
		return res;
	}

	/**
	 * Lookups bootstrap class without linking it.
	 *
	 * @param name Name of the class.
	 * @return class info or {@code null}, if not found.
	 */
	ClassParseResult lookup(String name) {
		return bootClassLoader.findBootClass(name);
	}

	/**
	 * Forcibly links bootstrap class.
	 *
	 * @param jc Class to link.
	 */
	void forceLink(InstanceJavaClass jc) {
		data.forceLinkClass(jc);
	}

	/**
	 * Returns class loader data.
	 *
	 * @return class loader data.
	 */
	ClassLoaderData getData() {
		return data;
	}
}
