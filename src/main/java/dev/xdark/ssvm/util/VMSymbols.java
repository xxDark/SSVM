package dev.xdark.ssvm.util;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.asm.Modifier;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.value.NullValue;
import lombok.val;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

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
	public final InstanceJavaClass java_lang_reflect_AccessibleObject;
	public final InstanceJavaClass java_security_PrivilegedExceptionAction;
	public final InstanceJavaClass java_lang_invoke_MemberName;
	public final InstanceJavaClass java_lang_invoke_ResolvedMethodName;
	public final InstanceJavaClass java_util_concurrent_atomic_AtomicLong;
	public final InstanceJavaClass java_lang_ClassLoader$NativeLibrary;
	public final InstanceJavaClass java_io_FileDescriptor;
	public final InstanceJavaClass java_lang_ArrayStoreException;
	public final InstanceJavaClass java_util_zip_ZipFile;
	public final InstanceJavaClass java_lang_IllegalMonitorStateException;
	public final InstanceJavaClass sun_management_VMManagementImpl;
	public final InstanceJavaClass java_lang_Package;
	public final InstanceJavaClass java_lang_invoke_MethodHandle;
	public final InstanceJavaClass perf_Perf;
	public final InstanceJavaClass java_nio_ByteBuffer;
	public final InstanceJavaClass java_util_jar_JarFile;
	public final InstanceJavaClass java_lang_StrictMath;
	public final InstanceJavaClass java_util_TimeZone;
	public final InstanceJavaClass java_util_zip_CRC32;
	public final InstanceJavaClass sun_security_provider_NativeSeedGenerator;
	public final InstanceJavaClass java_net_NetworkInterface;
	public final InstanceJavaClass sun_security_provider_SeedGenerator;
	public final InstanceJavaClass java_lang_invoke_MethodHandles;
	public final InstanceJavaClass java_lang_invoke_MethodHandles$Lookup;
	public final InstanceJavaClass reflect_ConstantPool;
	public final InstanceJavaClass java_lang_reflect_Proxy;
	public final InstanceJavaClass java_util_zip_Inflater;
	public final InstanceJavaClass java_lang_invoke_CallSite;
	public final InstanceJavaClass java_lang_ProcessEnvironment;
	public final InstanceJavaClass java_lang_InstantiationException;

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
		InstanceJavaClass reflectionFactory = (InstanceJavaClass) vm.findBootstrapClass("jdk/internal/reflect/ReflectionFactory");
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
		java_lang_reflect_AccessibleObject = (InstanceJavaClass) vm.findBootstrapClass("java/lang/reflect/AccessibleObject");
		java_security_PrivilegedExceptionAction = (InstanceJavaClass) vm.findBootstrapClass("java/security/PrivilegedExceptionAction");
		java_lang_invoke_MemberName = (InstanceJavaClass) vm.findBootstrapClass("java/lang/invoke/MemberName");
		java_lang_invoke_ResolvedMethodName = resolvedMemberName(vm);
		java_util_concurrent_atomic_AtomicLong = (InstanceJavaClass) vm.findBootstrapClass("java/util/concurrent/atomic/AtomicLong");
		java_lang_ClassLoader$NativeLibrary = (InstanceJavaClass) vm.findBootstrapClass("java/lang/ClassLoader$NativeLibrary");
		java_io_FileDescriptor = (InstanceJavaClass) vm.findBootstrapClass("java/io/FileDescriptor");
		java_lang_ArrayStoreException = (InstanceJavaClass) vm.findBootstrapClass("java/lang/ArrayStoreException");
		java_util_zip_ZipFile = (InstanceJavaClass) vm.findBootstrapClass("java/util/zip/ZipFile");
		java_lang_IllegalMonitorStateException = (InstanceJavaClass) vm.findBootstrapClass("java/lang/IllegalMonitorStateException");
		sun_management_VMManagementImpl = (InstanceJavaClass) vm.findBootstrapClass("sun/management/VMManagementImpl");
		java_lang_Package = (InstanceJavaClass) vm.findBootstrapClass("java/lang/Package");
		java_lang_invoke_MethodHandle = (InstanceJavaClass) vm.findBootstrapClass("java/lang/invoke/MethodHandle");

		InstanceJavaClass perf_Perf = (InstanceJavaClass) vm.findBootstrapClass("jdk/internal/perf/Perf");
		if (perf_Perf == null) {
			perf_Perf = (InstanceJavaClass) vm.findBootstrapClass("sun/misc/Perf");
		}
		this.perf_Perf = perf_Perf;
		java_nio_ByteBuffer = (InstanceJavaClass) vm.findBootstrapClass("java/nio/ByteBuffer");
		java_util_jar_JarFile = (InstanceJavaClass) vm.findBootstrapClass("java/util/jar/JarFile");
		java_lang_StrictMath = (InstanceJavaClass) vm.findBootstrapClass("java/lang/StrictMath");
		java_util_TimeZone = (InstanceJavaClass) vm.findBootstrapClass("java/util/TimeZone");
		java_util_zip_CRC32 = (InstanceJavaClass) vm.findBootstrapClass("java/util/zip/CRC32");
		sun_security_provider_NativeSeedGenerator = (InstanceJavaClass) vm.findBootstrapClass("sun/security/provider/NativeSeedGenerator");
		java_net_NetworkInterface = (InstanceJavaClass) vm.findBootstrapClass("java/net/NetworkInterface");
		sun_security_provider_SeedGenerator = (InstanceJavaClass) vm.findBootstrapClass("sun/security/provider/SeedGenerator");
		java_lang_invoke_MethodHandles = (InstanceJavaClass) vm.findBootstrapClass("java/lang/invoke/MethodHandles");
		java_lang_invoke_MethodHandles$Lookup = (InstanceJavaClass) vm.findBootstrapClass("java/lang/invoke/MethodHandles$Lookup");
		InstanceJavaClass reflect_ConstantPool = (InstanceJavaClass) vm.findBootstrapClass("jdk/internal/reflect/ConstantPool");
		if (reflect_ConstantPool == null) {
			reflect_ConstantPool = (InstanceJavaClass) vm.findBootstrapClass("sun/reflect/ConstantPool");
		}
		this.reflect_ConstantPool = reflect_ConstantPool;
		java_lang_reflect_Proxy = (InstanceJavaClass) vm.findBootstrapClass("java/lang/reflect/Proxy");
		java_util_zip_Inflater = (InstanceJavaClass) vm.findBootstrapClass("java/util/zip/Inflater");
		java_lang_invoke_CallSite = (InstanceJavaClass) vm.findBootstrapClass("java/lang/invoke/CallSite");
		java_lang_ProcessEnvironment = (InstanceJavaClass) vm.findBootstrapClass("java/lang/ProcessEnvironment");
		java_lang_InstantiationException = (InstanceJavaClass) vm.findBootstrapClass("java/lang/InstantiationException");
	}

	private static InstanceJavaClass resolvedMemberName(VirtualMachine vm) {
		InstanceJavaClass jc = (InstanceJavaClass) vm.findBootstrapClass("java/lang/invoke/MemberName$ResolvedMethodName");
		if (jc == null) {
			val writer = new ClassWriter(0);
			writer.visit(Opcodes.V1_8, Modifier.ACC_VM_HIDDEN, "java/lang/invoke/MemberName$ResolvedMethodName", null, "java/lang/Object", null);
			val b = writer.toByteArray();
			jc = vm.getHelper().defineClass(NullValue.INSTANCE, "java/lang/invoke/MemberName$ResolvedMethodName", b, 0, b.length, NullValue.INSTANCE, "JVM_DefineClass");
		}
		return jc;
	}
}
