package dev.xdark.ssvm.symbol;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.asm.Modifier;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.value.NullValue;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

/**
 * Implementation of initialized VM symbols.
 *
 * @author xDark
 */
public  final class InitializedVMSymbols implements VMSymbols {

	private final InstanceJavaClass java_lang_Object;
	private final InstanceJavaClass java_lang_Class;
	private final InstanceJavaClass java_lang_String;
	private final InstanceJavaClass java_lang_ClassLoader;
	private final InstanceJavaClass java_lang_Thread;
	private final InstanceJavaClass java_lang_ThreadGroup;
	private final InstanceJavaClass java_lang_System;
	private final InstanceJavaClass java_lang_Throwable;
	private final InstanceJavaClass java_lang_Error;
	private final InstanceJavaClass java_lang_Exception;
	private final InstanceJavaClass java_lang_NullPointerException;
	private final InstanceJavaClass java_lang_NoSuchFieldError;
	private final InstanceJavaClass java_lang_NoSuchMethodError;
	private final InstanceJavaClass java_lang_ArrayIndexOutOfBoundsException;
	private final InstanceJavaClass java_lang_ExceptionInInitializerError;
	private final InstanceJavaClass java_lang_UnsatisfiedLinkError;
	private final InstanceJavaClass java_lang_InternalError;
	private final InstanceJavaClass java_lang_ClassCastException;
	private final InstanceJavaClass java_lang_invoke_MethodHandleNatives;
	private final InstanceJavaClass java_lang_NoClassDefFoundError;
	private final InstanceJavaClass java_lang_ClassNotFoundException;
	private final InstanceJavaClass java_util_Vector;
	private final InstanceJavaClass java_lang_OutOfMemoryError;
	private final InstanceJavaClass java_lang_NegativeArraySizeException;
	private final InstanceJavaClass java_lang_IllegalArgumentException;
	private final InstanceJavaClass java_lang_AbstractMethodError;
	private final InstanceJavaClass java_lang_reflect_Array;
	private final InstanceJavaClass java_lang_BootstrapMethodError;
	private final InstanceJavaClass java_lang_IllegalStateException;
	private final InstanceJavaClass java_lang_NoSuchMethodException;
	private final InstanceJavaClass java_lang_InterruptedException;
	private final InstanceJavaClass java_lang_StackTraceElement;
	private final InstanceJavaClass java_security_PrivilegedAction;
	private final InstanceJavaClass reflect_ReflectionFactory;
	private final InstanceJavaClass java_lang_reflect_Constructor;
	private final InstanceJavaClass java_lang_reflect_Method;
	private final InstanceJavaClass java_lang_reflect_Field;
	private final InstanceJavaClass java_lang_Long;
	private final InstanceJavaClass java_lang_Double;
	private final InstanceJavaClass java_lang_Integer;
	private final InstanceJavaClass java_lang_Float;
	private final InstanceJavaClass java_lang_Character;
	private final InstanceJavaClass java_lang_Short;
	private final InstanceJavaClass java_lang_Byte;
	private final InstanceJavaClass java_lang_Boolean;
	private final InstanceJavaClass java_io_IOException;
	private final InstanceJavaClass java_lang_invoke_MethodType;
	private final InstanceJavaClass java_lang_reflect_AccessibleObject;
	private final InstanceJavaClass java_security_PrivilegedExceptionAction;
	private final InstanceJavaClass java_lang_invoke_MemberName;
	private final InstanceJavaClass java_lang_invoke_ResolvedMethodName;
	private final InstanceJavaClass java_util_concurrent_atomic_AtomicLong;
	private final InstanceJavaClass java_io_FileDescriptor;
	private final InstanceJavaClass java_lang_ArrayStoreException;
	private final InstanceJavaClass java_util_zip_ZipFile;
	private final InstanceJavaClass java_lang_IllegalMonitorStateException;
	private final InstanceJavaClass sun_management_VMManagementImpl;
	private final InstanceJavaClass java_lang_Package;
	private final InstanceJavaClass java_lang_invoke_MethodHandle;
	private final InstanceJavaClass perf_Perf;
	private final InstanceJavaClass java_nio_ByteBuffer;
	private final InstanceJavaClass java_util_jar_JarFile;
	private final InstanceJavaClass java_lang_StrictMath;
	private final InstanceJavaClass java_util_TimeZone;
	private final InstanceJavaClass java_util_zip_CRC32;
	private final InstanceJavaClass sun_security_provider_NativeSeedGenerator;
	private final InstanceJavaClass java_net_NetworkInterface;
	private final InstanceJavaClass sun_security_provider_SeedGenerator;
	private final InstanceJavaClass java_lang_invoke_MethodHandles;
	private final InstanceJavaClass java_lang_invoke_MethodHandles$Lookup;
	private final InstanceJavaClass reflect_ConstantPool;
	private final InstanceJavaClass java_lang_reflect_Proxy;
	private final InstanceJavaClass java_util_zip_Inflater;
	private final InstanceJavaClass java_lang_invoke_CallSite;
	private final InstanceJavaClass java_lang_ProcessEnvironment;
	private final InstanceJavaClass java_lang_InstantiationException;
	private final InstanceJavaClass reflect_MethodAccessorImpl;
	private final InstanceJavaClass java_util_zip_ZipException;
	private final InstanceJavaClass java_lang_IllegalAccessException;
	private final InstanceJavaClass java_lang_Module;
	private final InstanceJavaClass java_io_Serializable;
	private final InstanceJavaClass java_lang_Cloneable;
	private final InstanceJavaClass java_lang_IncompatibleClassChangeError;
	private final InstanceJavaClass java_io_FileNotFoundException;

	/**
	 * @param vm
	 * 		VM instance.
	 */
	public InitializedVMSymbols(VirtualMachine vm) {
		vm.assertInitialized();
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
		InstanceJavaClass reflect_MethodAccessorImpl = (InstanceJavaClass) vm.findBootstrapClass("jdk/internal/reflect/MethodAccessorImpl");
		if (reflect_MethodAccessorImpl == null) {
			reflect_MethodAccessorImpl = (InstanceJavaClass) vm.findBootstrapClass("sun/reflect/MethodAccessorImpl");
		}
		this.reflect_MethodAccessorImpl = reflect_MethodAccessorImpl;
		java_util_zip_ZipException = (InstanceJavaClass) vm.findBootstrapClass("java/util/zip/ZipException");
		java_lang_IllegalAccessException = (InstanceJavaClass) vm.findBootstrapClass("java/lang/IllegalAccessException");
		java_lang_Module = (InstanceJavaClass) vm.findBootstrapClass("java/lang/Module");
		java_io_Serializable = (InstanceJavaClass) vm.findBootstrapClass("java/io/Serializable");
		java_lang_Cloneable = (InstanceJavaClass) vm.findBootstrapClass("java/lang/Cloneable");
		java_lang_IncompatibleClassChangeError = (InstanceJavaClass) vm.findBootstrapClass("java/lang/IncompatibleClassChangeError");
		java_io_FileNotFoundException = (InstanceJavaClass) vm.findBootstrapClass("java/io/FileNotFoundException");
	}

	@Override
	public InstanceJavaClass java_lang_Object() {
		return java_lang_Object;
	}

	@Override
	public InstanceJavaClass java_lang_Class() {
		return java_lang_Class;
	}

	@Override
	public InstanceJavaClass java_lang_String() {
		return java_lang_String;
	}

	@Override
	public InstanceJavaClass java_lang_ClassLoader() {
		return java_lang_ClassLoader;
	}

	@Override
	public InstanceJavaClass java_lang_Thread() {
		return java_lang_Thread;
	}

	@Override
	public InstanceJavaClass java_lang_ThreadGroup() {
		return java_lang_ThreadGroup;
	}

	@Override
	public InstanceJavaClass java_lang_System() {
		return java_lang_System;
	}

	@Override
	public InstanceJavaClass java_lang_Throwable() {
		return java_lang_Throwable;
	}

	@Override
	public InstanceJavaClass java_lang_Error() {
		return java_lang_Error;
	}

	@Override
	public InstanceJavaClass java_lang_Exception() {
		return java_lang_Exception;
	}

	@Override
	public InstanceJavaClass java_lang_NullPointerException() {
		return java_lang_NullPointerException;
	}

	@Override
	public InstanceJavaClass java_lang_NoSuchFieldError() {
		return java_lang_NoSuchFieldError;
	}

	@Override
	public InstanceJavaClass java_lang_NoSuchMethodError() {
		return java_lang_NoSuchMethodError;
	}

	@Override
	public InstanceJavaClass java_lang_ArrayIndexOutOfBoundsException() {
		return java_lang_ArrayIndexOutOfBoundsException;
	}

	@Override
	public InstanceJavaClass java_lang_ExceptionInInitializerError() {
		return java_lang_ExceptionInInitializerError;
	}

	@Override
	public InstanceJavaClass java_lang_UnsatisfiedLinkError() {
		return java_lang_UnsatisfiedLinkError;
	}

	@Override
	public InstanceJavaClass java_lang_InternalError() {
		return java_lang_InternalError;
	}

	@Override
	public InstanceJavaClass java_lang_ClassCastException() {
		return java_lang_ClassCastException;
	}

	@Override
	public InstanceJavaClass java_lang_invoke_MethodHandleNatives() {
		return java_lang_invoke_MethodHandleNatives;
	}

	@Override
	public InstanceJavaClass java_lang_NoClassDefFoundError() {
		return java_lang_NoClassDefFoundError;
	}

	@Override
	public InstanceJavaClass java_lang_ClassNotFoundException() {
		return java_lang_ClassNotFoundException;
	}

	@Override
	public InstanceJavaClass java_util_Vector() {
		return java_util_Vector;
	}

	@Override
	public InstanceJavaClass java_lang_OutOfMemoryError() {
		return java_lang_OutOfMemoryError;
	}

	@Override
	public InstanceJavaClass java_lang_NegativeArraySizeException() {
		return java_lang_NegativeArraySizeException;
	}

	@Override
	public InstanceJavaClass java_lang_IllegalArgumentException() {
		return java_lang_IllegalArgumentException;
	}

	@Override
	public InstanceJavaClass java_lang_AbstractMethodError() {
		return java_lang_AbstractMethodError;
	}

	@Override
	public InstanceJavaClass java_lang_reflect_Array() {
		return java_lang_reflect_Array;
	}

	@Override
	public InstanceJavaClass java_lang_BootstrapMethodError() {
		return java_lang_BootstrapMethodError;
	}

	@Override
	public InstanceJavaClass java_lang_IllegalStateException() {
		return java_lang_IllegalStateException;
	}

	@Override
	public InstanceJavaClass java_lang_NoSuchMethodException() {
		return java_lang_NoSuchMethodException;
	}

	@Override
	public InstanceJavaClass java_lang_InterruptedException() {
		return java_lang_InterruptedException;
	}

	@Override
	public InstanceJavaClass java_lang_StackTraceElement() {
		return java_lang_StackTraceElement;
	}

	@Override
	public InstanceJavaClass java_security_PrivilegedAction() {
		return java_security_PrivilegedAction;
	}

	@Override
	public InstanceJavaClass reflect_ReflectionFactory() {
		return reflect_ReflectionFactory;
	}

	@Override
	public InstanceJavaClass java_lang_reflect_Constructor() {
		return java_lang_reflect_Constructor;
	}

	@Override
	public InstanceJavaClass java_lang_reflect_Method() {
		return java_lang_reflect_Method;
	}

	@Override
	public InstanceJavaClass java_lang_reflect_Field() {
		return java_lang_reflect_Field;
	}

	@Override
	public InstanceJavaClass java_lang_Long() {
		return java_lang_Long;
	}

	@Override
	public InstanceJavaClass java_lang_Double() {
		return java_lang_Double;
	}

	@Override
	public InstanceJavaClass java_lang_Integer() {
		return java_lang_Integer;
	}

	@Override
	public InstanceJavaClass java_lang_Float() {
		return java_lang_Float;
	}

	@Override
	public InstanceJavaClass java_lang_Character() {
		return java_lang_Character;
	}

	@Override
	public InstanceJavaClass java_lang_Short() {
		return java_lang_Short;
	}

	@Override
	public InstanceJavaClass java_lang_Byte() {
		return java_lang_Byte;
	}

	@Override
	public InstanceJavaClass java_lang_Boolean() {
		return java_lang_Boolean;
	}

	@Override
	public InstanceJavaClass java_io_IOException() {
		return java_io_IOException;
	}

	@Override
	public InstanceJavaClass java_lang_invoke_MethodType() {
		return java_lang_invoke_MethodType;
	}

	@Override
	public InstanceJavaClass java_lang_reflect_AccessibleObject() {
		return java_lang_reflect_AccessibleObject;
	}

	@Override
	public InstanceJavaClass java_security_PrivilegedExceptionAction() {
		return java_security_PrivilegedExceptionAction;
	}

	@Override
	public InstanceJavaClass java_lang_invoke_MemberName() {
		return java_lang_invoke_MemberName;
	}

	@Override
	public InstanceJavaClass java_lang_invoke_ResolvedMethodName() {
		return java_lang_invoke_ResolvedMethodName;
	}

	@Override
	public InstanceJavaClass java_util_concurrent_atomic_AtomicLong() {
		return java_util_concurrent_atomic_AtomicLong;
	}

	@Override
	public InstanceJavaClass java_io_FileDescriptor() {
		return java_io_FileDescriptor;
	}

	@Override
	public InstanceJavaClass java_lang_ArrayStoreException() {
		return java_lang_ArrayStoreException;
	}

	@Override
	public InstanceJavaClass java_util_zip_ZipFile() {
		return java_util_zip_ZipFile;
	}

	@Override
	public InstanceJavaClass java_lang_IllegalMonitorStateException() {
		return java_lang_IllegalMonitorStateException;
	}

	@Override
	public InstanceJavaClass sun_management_VMManagementImpl() {
		return sun_management_VMManagementImpl;
	}

	@Override
	public InstanceJavaClass java_lang_Package() {
		return java_lang_Package;
	}

	@Override
	public InstanceJavaClass java_lang_invoke_MethodHandle() {
		return java_lang_invoke_MethodHandle;
	}

	@Override
	public InstanceJavaClass perf_Perf() {
		return perf_Perf;
	}

	@Override
	public InstanceJavaClass java_nio_ByteBuffer() {
		return java_nio_ByteBuffer;
	}

	@Override
	public InstanceJavaClass java_util_jar_JarFile() {
		return java_util_jar_JarFile;
	}

	@Override
	public InstanceJavaClass java_lang_StrictMath() {
		return java_lang_StrictMath;
	}

	@Override
	public InstanceJavaClass java_util_TimeZone() {
		return java_util_TimeZone;
	}

	@Override
	public InstanceJavaClass java_util_zip_CRC32() {
		return java_util_zip_CRC32;
	}

	@Override
	public InstanceJavaClass sun_security_provider_NativeSeedGenerator() {
		return sun_security_provider_NativeSeedGenerator;
	}

	@Override
	public InstanceJavaClass java_net_NetworkInterface() {
		return java_net_NetworkInterface;
	}

	@Override
	public InstanceJavaClass sun_security_provider_SeedGenerator() {
		return sun_security_provider_SeedGenerator;
	}

	@Override
	public InstanceJavaClass java_lang_invoke_MethodHandles() {
		return java_lang_invoke_MethodHandles;
	}

	@Override
	public InstanceJavaClass java_lang_invoke_MethodHandles$Lookup() {
		return java_lang_invoke_MethodHandles$Lookup;
	}

	@Override
	public InstanceJavaClass reflect_ConstantPool() {
		return reflect_ConstantPool;
	}

	@Override
	public InstanceJavaClass java_lang_reflect_Proxy() {
		return java_lang_reflect_Proxy;
	}

	@Override
	public InstanceJavaClass java_util_zip_Inflater() {
		return java_util_zip_Inflater;
	}

	@Override
	public InstanceJavaClass java_lang_invoke_CallSite() {
		return java_lang_invoke_CallSite;
	}

	@Override
	public InstanceJavaClass java_lang_ProcessEnvironment() {
		return java_lang_ProcessEnvironment;
	}

	@Override
	public InstanceJavaClass java_lang_InstantiationException() {
		return java_lang_InstantiationException;
	}

	@Override
	public InstanceJavaClass reflect_MethodAccessorImpl() {
		return reflect_MethodAccessorImpl;
	}

	@Override
	public InstanceJavaClass java_util_zip_ZipException() {
		return java_util_zip_ZipException;
	}

	@Override
	public InstanceJavaClass java_lang_IllegalAccessException() {
		return java_lang_IllegalAccessException;
	}

	@Override
	public InstanceJavaClass java_lang_Module() {
		return java_lang_Module;
	}

	@Override
	public InstanceJavaClass java_io_Serializable() {
		return java_io_Serializable;
	}

	@Override
	public InstanceJavaClass java_lang_Cloneable() {
		return java_lang_Cloneable;
	}

	@Override
	public InstanceJavaClass java_lang_IncompatibleClassChangeError() {
		return java_lang_IncompatibleClassChangeError;
	}

	@Override
	public InstanceJavaClass java_io_FileNotFoundException() {
		return java_io_FileNotFoundException;
	}

	private static InstanceJavaClass resolvedMemberName(VirtualMachine vm) {
		InstanceJavaClass jc = (InstanceJavaClass) vm.findBootstrapClass("java/lang/invoke/ResolvedMethodName");
		if (jc == null) {
			ClassWriter writer = new ClassWriter(0);
			writer.visit(Opcodes.V1_8, Modifier.ACC_VM_HIDDEN, "java/lang/invoke/ResolvedMethodName", null, "java/lang/Object", null);
			byte[] b = writer.toByteArray();
			jc = vm.getHelper().defineClass(NullValue.INSTANCE, "java/lang/invoke/ResolvedMethodName", b, 0, b.length, NullValue.INSTANCE, "JVM_DefineClass");
		}
		return jc;
	}
}
