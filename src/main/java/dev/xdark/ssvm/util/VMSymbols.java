package dev.xdark.ssvm.util;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.mirror.InstanceJavaClass;

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
	public final InstanceJavaClass java_lang_Throwable;
	public final InstanceJavaClass java_lang_Error;
	public final InstanceJavaClass java_lang_Exception;
	public final InstanceJavaClass java_lang_NullPointerException;
	public final InstanceJavaClass java_lang_NoSuchFieldError;
	public final InstanceJavaClass java_lang_NoSuchMethodError;
	public final InstanceJavaClass java_lang_ArrayIndexOutOfBoundsException;
	public final InstanceJavaClass java_lang_ExceptionInInitializerError;

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
		java_lang_Throwable = (InstanceJavaClass) vm.findBootstrapClass("java/lang/Throwable", false);
		java_lang_Error = (InstanceJavaClass) vm.findBootstrapClass("java/lang/Error", false);
		java_lang_Exception = (InstanceJavaClass) vm.findBootstrapClass("java/lang/Exception", false);
		java_lang_NullPointerException = (InstanceJavaClass) vm.findBootstrapClass("java/lang/NullPointerException", false);
		java_lang_NoSuchFieldError = (InstanceJavaClass) vm.findBootstrapClass("java/lang/NoSuchFieldError", false);
		java_lang_NoSuchMethodError = (InstanceJavaClass) vm.findBootstrapClass("java/lang/NoSuchMethodError", false);
		java_lang_ArrayIndexOutOfBoundsException = (InstanceJavaClass) vm.findBootstrapClass("java/lang/ArrayIndexOutOfBoundsException", false);
		java_lang_ExceptionInInitializerError = (InstanceJavaClass) vm.findBootstrapClass("java/lang/ExceptionInInitializerError", false);

	}
}
