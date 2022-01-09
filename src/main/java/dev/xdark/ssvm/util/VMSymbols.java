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
	public final InstanceJavaClass java_lang_Class;
	public final InstanceJavaClass java_lang_String;
	public final InstanceJavaClass java_lang_ClassLoader;
	public final InstanceJavaClass java_lang_Thread;
	public final InstanceJavaClass java_lang_ThreadGroup;
	public final InstanceJavaClass java_lang_System;

	/**
	 * @param vm
	 * 		VM instance.
	 */
	public VMSymbols(VirtualMachine vm) {
		java_lang_Object = (InstanceJavaClass) vm.findBootstrapClass("java/lang/Object", false);
		java_lang_Class = (InstanceJavaClass) vm.findBootstrapClass("java/lang/Class", false);
		java_lang_String = (InstanceJavaClass) vm.findBootstrapClass("java/lang/String", false);
		java_lang_ClassLoader = (InstanceJavaClass) vm.findBootstrapClass("java/lang/ClassLoader", false);
		java_lang_Thread = (InstanceJavaClass) vm.findBootstrapClass("java/lang/Thread", false);
		java_lang_ThreadGroup = (InstanceJavaClass) vm.findBootstrapClass("java/lang/ThreadGroup", false);
		java_lang_System = (InstanceJavaClass) vm.findBootstrapClass("java/lang/System", false);
	}
}
