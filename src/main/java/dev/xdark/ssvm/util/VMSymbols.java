package dev.xdark.ssvm.util;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.mirror.JavaClass;

/**
 * Common VM symbols.
 *
 * @author xDark
 */
public final class VMSymbols {

	public final InstanceJavaClass java_lang_Object;
	public final InstanceJavaClass java_lang_String;

	/**
	 * @param vm
	 * 		VM instance.
	 *
	 * @throws Exception
	 * 		If vm cannot locate classes.
	 */
	public VMSymbols(VirtualMachine vm) throws Exception {
		java_lang_Object = (InstanceJavaClass) vm.findBootstrapClass("java/lang/Object", true);
		java_lang_String = (InstanceJavaClass) vm.findBootstrapClass("java/lang/String", true);
	}
}
