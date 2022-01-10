package dev.xdark.ssvm;

import dev.xdark.ssvm.classloading.BootClassLoader;
import dev.xdark.ssvm.classloading.ClassLoaderData;
import dev.xdark.ssvm.classloading.ClassParseResult;
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
	private InstanceJavaClass jc;

	/**
	 * Boot class loader to get classes from.
	 *
	 * @param vm
	 * 		VM instance.
	 * @param bootClassLoader
	 * 		Boot class loader.
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
	 */
	JavaClass findBootClass(String name) {
		var dimensions = 0;
		while (name.charAt(dimensions) == '[') dimensions++;
		var data = this.data;
		var trueName = dimensions == 0 ? name : name.substring(dimensions + 1, name.length() - 1);
		synchronized (data) {
			var jc = data.getClass(trueName);
			if (jc == null) {
				var result = bootClassLoader.findBootClass(trueName);
				if (result == null) return null;
				var vm = this.vm;
				jc = new InstanceJavaClass(vm, NullValue.INSTANCE, result.getClassReader(), result.getNode());
				var oop = vm.getMemoryManager().setOopForClass(jc);
				((InstanceJavaClass) jc).setOop(oop);
				data.linkClass(jc);
				vm.getHelper().initializeDefaultValues(oop, this.jc);
			}
			while (dimensions-- != 0) jc = jc.newArrayClass();
			return jc;
		}
	}

	/**
	 * Lookups bootstrap class without linking it.
	 *
	 * @param name
	 * 		Name of the class.
	 *
	 * @return class info or {@code null}, if not found.
	 */
	ClassParseResult lookup(String name) {
		return bootClassLoader.findBootClass(name);
	}

	/**
	 * Forcibly links bootstrap class.
	 *
	 * @param jc
	 * 		Class to link.
	 */
	void forceLink(JavaClass jc) {
		if ("java/lang/Class".equals(jc.getInternalName())) {
			this.jc = (InstanceJavaClass) jc;
		}
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
