package dev.xdark.ssvm.util;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.mirror.PrimitiveClass;

/**
 * Common VM symbols.
 *
 * @author xDark
 */
public final class VMSymbols {

	public final InstanceJavaClass java_lang_Object;
	public final InstanceJavaClass java_lang_String;
	public final InstanceJavaClass java_lang_ClassLoader;

	/**
	 * @param vm
	 * 		VM instance.
	 */
	public VMSymbols(VirtualMachine vm) {
		java_lang_Object = (InstanceJavaClass) vm.findBootstrapClass("java/lang/Object", true);
		java_lang_String = (InstanceJavaClass) vm.findBootstrapClass("java/lang/String", true);
		java_lang_ClassLoader = (InstanceJavaClass) vm.findBootstrapClass("java/lang/ClassLoader", true);
	}
}
