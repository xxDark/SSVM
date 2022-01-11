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
	public final InstanceJavaClass java_lang_UnsatisfiedLinkError;
	public final InstanceJavaClass java_lang_InternalError;
	public final InstanceJavaClass java_lang_ClassCastException;
	public final InstanceJavaClass java_lang_invoke_MethodHandleNatives;
	public final InstanceJavaClass java_lang_NoClassDefFoundError;
	public final InstanceJavaClass java_lang_ClassNotFoundException;
	public final InstanceJavaClass java_util_Vector;
	public final InstanceJavaClass java_lang_OutOfMemoryError;
	public final InstanceJavaClass java_lang_NegativeArraySizeException;
	public final InstanceJavaClass java_lang_IllegalArgumentException;
	public final InstanceJavaClass java_lang_AbstractMethodError;
	public final InstanceJavaClass java_lang_reflect_Array;
	public final InstanceJavaClass java_lang_BootstrapMethodError;
	public final InstanceJavaClass java_lang_IllegalStateException;
	public final InstanceJavaClass java_lang_NoSuchMethodException;
	public final InstanceJavaClass java_lang_InterruptedException;
	public final InstanceJavaClass java_lang_StackTraceElement;
	public final InstanceJavaClass java_security_PrivilegedAction;
	public final InstanceJavaClass reflect_ReflectionFactory;
	public final InstanceJavaClass java_lang_reflect_Constructor;
	public final InstanceJavaClass java_lang_reflect_Method;
	public final InstanceJavaClass java_lang_reflect_Field;
	public final InstanceJavaClass java_lang_Long;
	public final InstanceJavaClass java_lang_Double;
	public final InstanceJavaClass java_lang_Integer;
	public final InstanceJavaClass java_lang_Float;
	public final InstanceJavaClass java_lang_Character;
	public final InstanceJavaClass java_lang_Short;
	public final InstanceJavaClass java_lang_Byte;
	public final InstanceJavaClass java_lang_Boolean;
	public final InstanceJavaClass java_io_IOException;
	public final InstanceJavaClass java_lang_invoke_MethodType;

	/**
	 * @param vm
	 * 		VM instance.
	 */
	public VMSymbols(VirtualMachine vm) {
		java_lang_Object = (InstanceJavaClass) vm.findBootstrapClass("java/lang/Object");
		java_lang_Class = (InstanceJavaClass) vm.findBootstrapClass("java/lang/Class");
		java_lang_String = (InstanceJavaClass) vm.findBootstrapClass("java/lang/String");
		java_lang_ClassLoader = (InstanceJavaClass) vm.findBootstrapClass("java/lang/ClassLoader");
		java_lang_Thread = (InstanceJavaClass) vm.findBootstrapClass("java/lang/Thread");
		java_lang_ThreadGroup = (InstanceJavaClass) vm.findBootstrapClass("java/lang/ThreadGroup");
		java_lang_System = (InstanceJavaClass) vm.findBootstrapClass("java/lang/System");
		java_lang_Throwable = (InstanceJavaClass) vm.findBootstrapClass("java/lang/Throwable");
		java_lang_Error = (InstanceJavaClass) vm.findBootstrapClass("java/lang/Error");
		java_lang_Exception = (InstanceJavaClass) vm.findBootstrapClass("java/lang/Exception");
		java_lang_NullPointerException = (InstanceJavaClass) vm.findBootstrapClass("java/lang/NullPointerException");
		java_lang_NoSuchFieldError = (InstanceJavaClass) vm.findBootstrapClass("java/lang/NoSuchFieldError");
		java_lang_NoSuchMethodError = (InstanceJavaClass) vm.findBootstrapClass("java/lang/NoSuchMethodError");
		java_lang_ArrayIndexOutOfBoundsException = (InstanceJavaClass) vm.findBootstrapClass("java/lang/ArrayIndexOutOfBoundsException");
		java_lang_ExceptionInInitializerError = (InstanceJavaClass) vm.findBootstrapClass("java/lang/ExceptionInInitializerError");
		java_lang_UnsatisfiedLinkError = (InstanceJavaClass) vm.findBootstrapClass("java/lang/UnsatisfiedLinkError");
		java_lang_InternalError = (InstanceJavaClass) vm.findBootstrapClass("java/lang/InternalError");
		java_lang_ClassCastException = (InstanceJavaClass) vm.findBootstrapClass("java/lang/ClassCastException");
		java_lang_invoke_MethodHandleNatives = (InstanceJavaClass) vm.findBootstrapClass("java/lang/invoke/MethodHandleNatives");
		java_lang_NoClassDefFoundError = (InstanceJavaClass) vm.findBootstrapClass("java/lang/NoClassDefFoundError");
		java_lang_ClassNotFoundException = (InstanceJavaClass) vm.findBootstrapClass("java/lang/ClassNotFoundException");
		java_util_Vector = (InstanceJavaClass) vm.findBootstrapClass("java/util/Vector");
		java_lang_OutOfMemoryError = (InstanceJavaClass) vm.findBootstrapClass("java/lang/OutOfMemoryError");
		java_lang_NegativeArraySizeException = (InstanceJavaClass) vm.findBootstrapClass("java/lang/NegativeArraySizeException");
		java_lang_IllegalArgumentException = (InstanceJavaClass) vm.findBootstrapClass("java/lang/IllegalArgumentException");
		java_lang_AbstractMethodError = (InstanceJavaClass) vm.findBootstrapClass("java/lang/AbstractMethodError");
		java_lang_reflect_Array = (InstanceJavaClass) vm.findBootstrapClass("java/lang/reflect/Array");
		java_lang_BootstrapMethodError = (InstanceJavaClass) vm.findBootstrapClass("java/lang/BootstrapMethodError");
		java_lang_IllegalStateException = (InstanceJavaClass) vm.findBootstrapClass("java/lang/IllegalStateException");
		java_lang_NoSuchMethodException = (InstanceJavaClass) vm.findBootstrapClass("java/lang/NoSuchMethodException");
		java_lang_InterruptedException = (InstanceJavaClass) vm.findBootstrapClass("java/lang/InterruptedException");
		java_lang_StackTraceElement = (InstanceJavaClass) vm.findBootstrapClass("java/lang/StackTraceElement");
		java_security_PrivilegedAction = (InstanceJavaClass) vm.findBootstrapClass("java/security/PrivilegedAction");
		var reflectionFactory = (InstanceJavaClass) vm.findBootstrapClass("jdk/internal/reflect/ReflectionFactory");
		if (reflectionFactory == null) {
			reflectionFactory = (InstanceJavaClass) vm.findBootstrapClass("sun/reflect/ReflectionFactory");
		}
		reflect_ReflectionFactory = reflectionFactory;
		java_lang_reflect_Constructor = (InstanceJavaClass) vm.findBootstrapClass("java/lang/reflect/Constructor");
		java_lang_reflect_Method = (InstanceJavaClass) vm.findBootstrapClass("java/lang/reflect/Method");
		java_lang_reflect_Field = (InstanceJavaClass) vm.findBootstrapClass("java/lang/reflect/Field");
		java_lang_Long = (InstanceJavaClass) vm.findBootstrapClass("java/lang/Long");
		java_lang_Double = (InstanceJavaClass) vm.findBootstrapClass("java/lang/Double");
		java_lang_Integer = (InstanceJavaClass) vm.findBootstrapClass("java/lang/Integer");
		java_lang_Float = (InstanceJavaClass) vm.findBootstrapClass("java/lang/Float");
		java_lang_Character = (InstanceJavaClass) vm.findBootstrapClass("java/lang/Character");
		java_lang_Short = (InstanceJavaClass) vm.findBootstrapClass("java/lang/Short");
		java_lang_Byte = (InstanceJavaClass) vm.findBootstrapClass("java/lang/Byte");
		java_lang_Boolean = (InstanceJavaClass) vm.findBootstrapClass("java/lang/Boolean");
		java_io_IOException = (InstanceJavaClass) vm.findBootstrapClass("java/io/IOException");
		java_lang_invoke_MethodType = (InstanceJavaClass) vm.findBootstrapClass("java/lang/invoke/MethodType");
	}
}
