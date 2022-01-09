package dev.xdark.ssvm;

import dev.xdark.ssvm.classloading.BootClassLoader;
import dev.xdark.ssvm.classloading.ClassLoaderData;
import dev.xdark.ssvm.classloading.ClassParseResult;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.mirror.JavaClass;
import dev.xdark.ssvm.value.InstanceValue;
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
	 *  @param vm
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
		var data = this.data;
		var jc = data.getClass(name);
		if (jc == null) {
			var result = bootClassLoader.findBootClass(name);
			if (result == null) return null;
			var vm = this.vm;
			var $jc = new InstanceJavaClass(vm, NullValue.INSTANCE, result.getClassReader(), result.getNode(), null);
			var oop = (InstanceValue) vm.getMemoryManager().newOopForClass($jc);
			$jc.setOop(oop);
			data.linkClass($jc);
			vm.getHelper().initializeDefaultValues(oop, this.jc);
			return $jc;
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
}
