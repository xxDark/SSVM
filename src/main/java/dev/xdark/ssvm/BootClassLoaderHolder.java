package dev.xdark.ssvm;

import dev.xdark.ssvm.classloading.BootClassLoader;
import dev.xdark.ssvm.classloading.ClassLoaderData;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.mirror.JavaClass;
import dev.xdark.ssvm.value.NullValue;

/**
 * Holder for the boot class loader.
 *
 * @author xDark
 */
final class BootClassLoaderHolder {

	private final ClassLoaderData data = new ClassLoaderData();
	private final VirtualMachine vm;
	private final BootClassLoader bootClassLoader;

	/**
	 * Boot class loader to get classes from.
	 *
	 * @param vm
	 * 		VM instance.
	 * @param bootClassLoader
	 * 		User-defined boot class loader.
	 */
	BootClassLoaderHolder(VirtualMachine vm, BootClassLoader bootClassLoader) {
		this.vm = vm;
		this.bootClassLoader = bootClassLoader;
	}

	/**
	 * Resolves boot class by it's name.
	 *
	 * @param name
	 * 		Name of the class.
	 *
	 * @return Resolved class or {@code null}, if not found.
	 *
	 * @throws Exception
	 * 		if any error occurs.
	 */
	JavaClass findBootClass(String name) throws Exception {
		var data = this.data;
		var jc = data.getClass(name);
		if (jc == null) {
			var result = bootClassLoader.findBootClass(name);
			if (result == null) return null;
			jc = new InstanceJavaClass(vm, NullValue.INSTANCE, result.getClassReader(), result.getNode(), null);
			data.linkClass(jc);
		}
		return jc;
	}

	/**
	 * Lookups bootstrap class without linking it.
	 *
	 * @param name
	 * 		Name of the class.
	 *
	 * @return class info or {@code null}, if not found.
	 *
	 * @throws Exception
	 * 		If any error occurs.
	 */
	BootClassLoader.LookupResult lookup(String name) throws Exception {
		return bootClassLoader.findBootClass(name);
	}

	/**
	 * Forcibly links bootstrap class.
	 *
	 * @param jc
	 * 		Class to link.
	 */
	void forceLink(JavaClass jc) {
		data.forceLinkClass(jc);
	}
}
