package dev.xdark.ssvm;

import dev.xdark.ssvm.asm.Modifier;
import dev.xdark.ssvm.execution.PanicException;
import dev.xdark.ssvm.mirror.type.InstanceClass;
import dev.xdark.ssvm.mirror.type.JavaClass;
import dev.xdark.ssvm.symbol.Symbols;
import dev.xdark.ssvm.value.ObjectValue;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

import java.util.Arrays;

/**
 * Implementation of initialized VM symbols.
 *
 * @author xDark
 */
final class InitializedSymbols implements Symbols {

	private final InstanceClass java_lang_Object;
	private final InstanceClass java_lang_Class;
	private final InstanceClass java_lang_String;
	private final InstanceClass java_lang_ClassLoader;
	private final InstanceClass java_lang_Thread;
	private final InstanceClass java_lang_ThreadGroup;
	private final InstanceClass java_lang_System;
	private final InstanceClass java_lang_Throwable;
	private final InstanceClass java_lang_Error;
	private final InstanceClass java_lang_Exception;
	private final InstanceClass java_lang_NullPointerException;
	private final InstanceClass java_lang_NoSuchFieldError;
	private final InstanceClass java_lang_NoSuchMethodError;
	private final InstanceClass java_lang_ArrayIndexOutOfBoundsException;
	private final InstanceClass java_lang_ExceptionInInitializerError;
	private final InstanceClass java_lang_UnsatisfiedLinkError;
	private final InstanceClass java_lang_InternalError;
	private final InstanceClass java_lang_ClassCastException;
	private final InstanceClass java_lang_invoke_MethodHandleNatives;
	private final InstanceClass java_lang_NoClassDefFoundError;
	private final InstanceClass java_lang_ClassNotFoundException;
	private final InstanceClass java_util_Vector;
	private final InstanceClass java_lang_OutOfMemoryError;
	private final InstanceClass java_lang_NegativeArraySizeException;
	private final InstanceClass java_lang_IllegalArgumentException;
	private final InstanceClass java_lang_AbstractMethodError;
	private final InstanceClass java_lang_reflect_Array;
	private final InstanceClass java_lang_BootstrapMethodError;
	private final InstanceClass java_lang_IllegalStateException;
	private final InstanceClass java_lang_NoSuchMethodException;
	private final InstanceClass java_lang_InterruptedException;
	private final InstanceClass java_lang_StackTraceElement;
	private final InstanceClass java_security_PrivilegedAction;
	private final InstanceClass java_lang_reflect_Constructor;
	private final InstanceClass java_lang_reflect_Method;
	private final InstanceClass java_lang_reflect_Field;
	private final InstanceClass java_lang_Long;
	private final InstanceClass java_lang_Double;
	private final InstanceClass java_lang_Integer;
	private final InstanceClass java_lang_Float;
	private final InstanceClass java_lang_Character;
	private final InstanceClass java_lang_Short;
	private final InstanceClass java_lang_Byte;
	private final InstanceClass java_lang_Boolean;
	private final InstanceClass java_io_IOException;
	private final InstanceClass java_lang_invoke_MethodType;
	private final InstanceClass java_lang_reflect_AccessibleObject;
	private final InstanceClass java_security_PrivilegedExceptionAction;
	private final InstanceClass java_lang_invoke_MemberName;
	private final InstanceClass java_util_concurrent_atomic_AtomicLong;
	private final InstanceClass java_io_FileDescriptor;
	private final InstanceClass java_lang_ArrayStoreException;
	private final InstanceClass java_util_zip_ZipFile;
	private final InstanceClass java_lang_IllegalMonitorStateException;
	private final InstanceClass sun_management_VMManagementImpl;
	private final InstanceClass java_lang_Package;
	private final InstanceClass java_lang_invoke_MethodHandle;
	private final InstanceClass java_nio_ByteBuffer;
	private final InstanceClass java_util_jar_JarFile;
	private final InstanceClass java_lang_StrictMath;
	private final InstanceClass java_util_TimeZone;
	private final InstanceClass java_util_zip_CRC32;
	private final InstanceClass sun_security_provider_NativeSeedGenerator;
	private final InstanceClass java_net_NetworkInterface;
	private final InstanceClass sun_security_provider_SeedGenerator;
	private final InstanceClass java_lang_invoke_MethodHandles;
	private final InstanceClass java_lang_invoke_MethodHandles$Lookup;
	private final InstanceClass java_lang_reflect_Proxy;
	private final InstanceClass java_util_zip_Inflater;
	private final InstanceClass java_lang_invoke_CallSite;
	private final InstanceClass java_lang_ProcessEnvironment;
	private final InstanceClass java_lang_InstantiationException;
	private final InstanceClass java_util_zip_ZipException;
	private final InstanceClass java_lang_IllegalAccessException;
	private final InstanceClass java_io_Serializable;
	private final InstanceClass java_lang_Cloneable;
	private final InstanceClass java_lang_IncompatibleClassChangeError;
	private final InstanceClass java_io_FileNotFoundException;
	private final InstanceClass java_lang_InstantiationError;
	private final InstanceClass java_lang_ArithmeticException;
	private final InstanceClass java_io_FileOutputStream;
	private InstanceClass reflect_ReflectionFactory;
	private InstanceClass perf_Perf;
	private InstanceClass reflect_ConstantPool;
	private InstanceClass internal_reflect_Reflection;
	private InstanceClass java_lang_invoke_ResolvedMethodName;
	private InstanceClass reflect_MethodAccessorImpl;

	/**
	 * @param vm VM instance.
	 */
	InitializedSymbols(VirtualMachine vm) {
		vm.assertInitialized();
		java_lang_Object = (InstanceClass) vm.findBootstrapClass("java/lang/Object");
		java_lang_Class = (InstanceClass) vm.findBootstrapClass("java/lang/Class");
		java_lang_String = (InstanceClass) vm.findBootstrapClass("java/lang/String");
		java_lang_ClassLoader = (InstanceClass) vm.findBootstrapClass("java/lang/ClassLoader");
		java_lang_Thread = (InstanceClass) vm.findBootstrapClass("java/lang/Thread");
		java_lang_ThreadGroup = (InstanceClass) vm.findBootstrapClass("java/lang/ThreadGroup");
		java_lang_System = (InstanceClass) vm.findBootstrapClass("java/lang/System");
		java_lang_Throwable = (InstanceClass) vm.findBootstrapClass("java/lang/Throwable");
		java_lang_Error = (InstanceClass) vm.findBootstrapClass("java/lang/Error");
		java_lang_Exception = (InstanceClass) vm.findBootstrapClass("java/lang/Exception");
		java_lang_NullPointerException = (InstanceClass) vm.findBootstrapClass("java/lang/NullPointerException");
		java_lang_NoSuchFieldError = (InstanceClass) vm.findBootstrapClass("java/lang/NoSuchFieldError");
		java_lang_NoSuchMethodError = (InstanceClass) vm.findBootstrapClass("java/lang/NoSuchMethodError");
		java_lang_ArrayIndexOutOfBoundsException = (InstanceClass) vm.findBootstrapClass("java/lang/ArrayIndexOutOfBoundsException");
		java_lang_ExceptionInInitializerError = (InstanceClass) vm.findBootstrapClass("java/lang/ExceptionInInitializerError");
		java_lang_UnsatisfiedLinkError = (InstanceClass) vm.findBootstrapClass("java/lang/UnsatisfiedLinkError");
		java_lang_InternalError = (InstanceClass) vm.findBootstrapClass("java/lang/InternalError");
		java_lang_ClassCastException = (InstanceClass) vm.findBootstrapClass("java/lang/ClassCastException");
		java_lang_invoke_MethodHandleNatives = (InstanceClass) vm.findBootstrapClass("java/lang/invoke/MethodHandleNatives");
		java_lang_NoClassDefFoundError = (InstanceClass) vm.findBootstrapClass("java/lang/NoClassDefFoundError");
		java_lang_ClassNotFoundException = (InstanceClass) vm.findBootstrapClass("java/lang/ClassNotFoundException");
		java_util_Vector = (InstanceClass) vm.findBootstrapClass("java/util/Vector");
		java_lang_OutOfMemoryError = (InstanceClass) vm.findBootstrapClass("java/lang/OutOfMemoryError");
		java_lang_NegativeArraySizeException = (InstanceClass) vm.findBootstrapClass("java/lang/NegativeArraySizeException");
		java_lang_IllegalArgumentException = (InstanceClass) vm.findBootstrapClass("java/lang/IllegalArgumentException");
		java_lang_AbstractMethodError = (InstanceClass) vm.findBootstrapClass("java/lang/AbstractMethodError");
		java_lang_reflect_Array = (InstanceClass) vm.findBootstrapClass("java/lang/reflect/Array");
		java_lang_BootstrapMethodError = (InstanceClass) vm.findBootstrapClass("java/lang/BootstrapMethodError");
		java_lang_IllegalStateException = (InstanceClass) vm.findBootstrapClass("java/lang/IllegalStateException");
		java_lang_NoSuchMethodException = (InstanceClass) vm.findBootstrapClass("java/lang/NoSuchMethodException");
		java_lang_InterruptedException = (InstanceClass) vm.findBootstrapClass("java/lang/InterruptedException");
		java_lang_StackTraceElement = (InstanceClass) vm.findBootstrapClass("java/lang/StackTraceElement");
		java_security_PrivilegedAction = (InstanceClass) vm.findBootstrapClass("java/security/PrivilegedAction");
		java_lang_reflect_Constructor = (InstanceClass) vm.findBootstrapClass("java/lang/reflect/Constructor");
		java_lang_reflect_Method = (InstanceClass) vm.findBootstrapClass("java/lang/reflect/Method");
		java_lang_reflect_Field = (InstanceClass) vm.findBootstrapClass("java/lang/reflect/Field");
		java_lang_Long = (InstanceClass) vm.findBootstrapClass("java/lang/Long");
		java_lang_Double = (InstanceClass) vm.findBootstrapClass("java/lang/Double");
		java_lang_Integer = (InstanceClass) vm.findBootstrapClass("java/lang/Integer");
		java_lang_Float = (InstanceClass) vm.findBootstrapClass("java/lang/Float");
		java_lang_Character = (InstanceClass) vm.findBootstrapClass("java/lang/Character");
		java_lang_Short = (InstanceClass) vm.findBootstrapClass("java/lang/Short");
		java_lang_Byte = (InstanceClass) vm.findBootstrapClass("java/lang/Byte");
		java_lang_Boolean = (InstanceClass) vm.findBootstrapClass("java/lang/Boolean");
		java_io_IOException = (InstanceClass) vm.findBootstrapClass("java/io/IOException");
		java_lang_invoke_MethodType = (InstanceClass) vm.findBootstrapClass("java/lang/invoke/MethodType");
		java_lang_reflect_AccessibleObject = (InstanceClass) vm.findBootstrapClass("java/lang/reflect/AccessibleObject");
		java_security_PrivilegedExceptionAction = (InstanceClass) vm.findBootstrapClass("java/security/PrivilegedExceptionAction");
		java_lang_invoke_MemberName = (InstanceClass) vm.findBootstrapClass("java/lang/invoke/MemberName");
		java_util_concurrent_atomic_AtomicLong = (InstanceClass) vm.findBootstrapClass("java/util/concurrent/atomic/AtomicLong");
		java_io_FileDescriptor = (InstanceClass) vm.findBootstrapClass("java/io/FileDescriptor");
		java_lang_ArrayStoreException = (InstanceClass) vm.findBootstrapClass("java/lang/ArrayStoreException");
		java_util_zip_ZipFile = (InstanceClass) vm.findBootstrapClass("java/util/zip/ZipFile");
		java_lang_IllegalMonitorStateException = (InstanceClass) vm.findBootstrapClass("java/lang/IllegalMonitorStateException");
		sun_management_VMManagementImpl = (InstanceClass) vm.findBootstrapClass("sun/management/VMManagementImpl");
		java_lang_Package = (InstanceClass) vm.findBootstrapClass("java/lang/Package");
		java_lang_invoke_MethodHandle = (InstanceClass) vm.findBootstrapClass("java/lang/invoke/MethodHandle");

		java_nio_ByteBuffer = (InstanceClass) vm.findBootstrapClass("java/nio/ByteBuffer");
		java_util_jar_JarFile = (InstanceClass) vm.findBootstrapClass("java/util/jar/JarFile");
		java_lang_StrictMath = (InstanceClass) vm.findBootstrapClass("java/lang/StrictMath");
		java_util_TimeZone = (InstanceClass) vm.findBootstrapClass("java/util/TimeZone");
		java_util_zip_CRC32 = (InstanceClass) vm.findBootstrapClass("java/util/zip/CRC32");
		sun_security_provider_NativeSeedGenerator = (InstanceClass) vm.findBootstrapClass("sun/security/provider/NativeSeedGenerator");
		java_net_NetworkInterface = (InstanceClass) vm.findBootstrapClass("java/net/NetworkInterface");
		sun_security_provider_SeedGenerator = (InstanceClass) vm.findBootstrapClass("sun/security/provider/SeedGenerator");
		java_lang_invoke_MethodHandles = (InstanceClass) vm.findBootstrapClass("java/lang/invoke/MethodHandles");
		java_lang_invoke_MethodHandles$Lookup = (InstanceClass) vm.findBootstrapClass("java/lang/invoke/MethodHandles$Lookup");
		java_lang_reflect_Proxy = (InstanceClass) vm.findBootstrapClass("java/lang/reflect/Proxy");
		java_util_zip_Inflater = (InstanceClass) vm.findBootstrapClass("java/util/zip/Inflater");
		java_lang_invoke_CallSite = (InstanceClass) vm.findBootstrapClass("java/lang/invoke/CallSite");
		java_lang_ProcessEnvironment = (InstanceClass) vm.findBootstrapClass("java/lang/ProcessEnvironment");
		java_lang_InstantiationException = (InstanceClass) vm.findBootstrapClass("java/lang/InstantiationException");
		java_util_zip_ZipException = (InstanceClass) vm.findBootstrapClass("java/util/zip/ZipException");
		java_lang_IllegalAccessException = (InstanceClass) vm.findBootstrapClass("java/lang/IllegalAccessException");
		java_io_Serializable = (InstanceClass) vm.findBootstrapClass("java/io/Serializable");
		java_lang_Cloneable = (InstanceClass) vm.findBootstrapClass("java/lang/Cloneable");
		java_lang_IncompatibleClassChangeError = (InstanceClass) vm.findBootstrapClass("java/lang/IncompatibleClassChangeError");
		java_io_FileNotFoundException = (InstanceClass) vm.findBootstrapClass("java/io/FileNotFoundException");
		java_lang_InstantiationError = (InstanceClass) vm.findBootstrapClass("java/lang/InstantiationError");
		java_lang_ArithmeticException = (InstanceClass) vm.findBootstrapClass("java/lang/ArithmeticException");
		java_io_FileOutputStream = (InstanceClass) vm.findBootstrapClass("java/io/FileOutputStream");
	}

	void postInit(VirtualMachine vm) {
		SafeClassLookup lookup = new SafeClassLookup(vm);
		reflect_ReflectionFactory = lookup.findBootstrapClass("jdk/internal/reflect/ReflectionFactory", "sun/reflect/ReflectionFactory");
		perf_Perf = lookup.findBootstrapClass("jdk/internal/perf/Perf", "sun/misc/Perf");
		reflect_ConstantPool = lookup.findBootstrapClass("jdk/internal/reflect/ConstantPool", "sun/reflect/ConstantPool");
		internal_reflect_Reflection = lookup.findBootstrapClass("jdk/internal/reflect/Reflection", "sun/reflect/Reflection");
		java_lang_invoke_ResolvedMethodName = resolvedMemberName(vm);
		reflect_MethodAccessorImpl = lookup.findBootstrapClass("jdk/internal/reflect/MethodAccessorImpl", "sun/reflect/MethodAccessorImpl");
	}

	@Override
	public InstanceClass java_lang_Object() {
		return java_lang_Object;
	}

	@Override
	public InstanceClass java_lang_Class() {
		return java_lang_Class;
	}

	@Override
	public InstanceClass java_lang_String() {
		return java_lang_String;
	}

	@Override
	public InstanceClass java_lang_ClassLoader() {
		return java_lang_ClassLoader;
	}

	@Override
	public InstanceClass java_lang_Thread() {
		return java_lang_Thread;
	}

	@Override
	public InstanceClass java_lang_ThreadGroup() {
		return java_lang_ThreadGroup;
	}

	@Override
	public InstanceClass java_lang_System() {
		return java_lang_System;
	}

	@Override
	public InstanceClass java_lang_Throwable() {
		return java_lang_Throwable;
	}

	@Override
	public InstanceClass java_lang_Error() {
		return java_lang_Error;
	}

	@Override
	public InstanceClass java_lang_Exception() {
		return java_lang_Exception;
	}

	@Override
	public InstanceClass java_lang_NullPointerException() {
		return java_lang_NullPointerException;
	}

	@Override
	public InstanceClass java_lang_NoSuchFieldError() {
		return java_lang_NoSuchFieldError;
	}

	@Override
	public InstanceClass java_lang_NoSuchMethodError() {
		return java_lang_NoSuchMethodError;
	}

	@Override
	public InstanceClass java_lang_ArrayIndexOutOfBoundsException() {
		return java_lang_ArrayIndexOutOfBoundsException;
	}

	@Override
	public InstanceClass java_lang_ExceptionInInitializerError() {
		return java_lang_ExceptionInInitializerError;
	}

	@Override
	public InstanceClass java_lang_UnsatisfiedLinkError() {
		return java_lang_UnsatisfiedLinkError;
	}

	@Override
	public InstanceClass java_lang_InternalError() {
		return java_lang_InternalError;
	}

	@Override
	public InstanceClass java_lang_ClassCastException() {
		return java_lang_ClassCastException;
	}

	@Override
	public InstanceClass java_lang_invoke_MethodHandleNatives() {
		return java_lang_invoke_MethodHandleNatives;
	}

	@Override
	public InstanceClass java_lang_NoClassDefFoundError() {
		return java_lang_NoClassDefFoundError;
	}

	@Override
	public InstanceClass java_lang_ClassNotFoundException() {
		return java_lang_ClassNotFoundException;
	}

	@Override
	public InstanceClass java_util_Vector() {
		return java_util_Vector;
	}

	@Override
	public InstanceClass java_lang_OutOfMemoryError() {
		return java_lang_OutOfMemoryError;
	}

	@Override
	public InstanceClass java_lang_NegativeArraySizeException() {
		return java_lang_NegativeArraySizeException;
	}

	@Override
	public InstanceClass java_lang_IllegalArgumentException() {
		return java_lang_IllegalArgumentException;
	}

	@Override
	public InstanceClass java_lang_AbstractMethodError() {
		return java_lang_AbstractMethodError;
	}

	@Override
	public InstanceClass java_lang_reflect_Array() {
		return java_lang_reflect_Array;
	}

	@Override
	public InstanceClass java_lang_BootstrapMethodError() {
		return java_lang_BootstrapMethodError;
	}

	@Override
	public InstanceClass java_lang_IllegalStateException() {
		return java_lang_IllegalStateException;
	}

	@Override
	public InstanceClass java_lang_NoSuchMethodException() {
		return java_lang_NoSuchMethodException;
	}

	@Override
	public InstanceClass java_lang_InterruptedException() {
		return java_lang_InterruptedException;
	}

	@Override
	public InstanceClass java_lang_StackTraceElement() {
		return java_lang_StackTraceElement;
	}

	@Override
	public InstanceClass java_security_PrivilegedAction() {
		return java_security_PrivilegedAction;
	}

	@Override
	public InstanceClass reflect_ReflectionFactory() {
		return reflect_ReflectionFactory;
	}

	@Override
	public InstanceClass java_lang_reflect_Constructor() {
		return java_lang_reflect_Constructor;
	}

	@Override
	public InstanceClass java_lang_reflect_Method() {
		return java_lang_reflect_Method;
	}

	@Override
	public InstanceClass java_lang_reflect_Field() {
		return java_lang_reflect_Field;
	}

	@Override
	public InstanceClass java_lang_Long() {
		return java_lang_Long;
	}

	@Override
	public InstanceClass java_lang_Double() {
		return java_lang_Double;
	}

	@Override
	public InstanceClass java_lang_Integer() {
		return java_lang_Integer;
	}

	@Override
	public InstanceClass java_lang_Float() {
		return java_lang_Float;
	}

	@Override
	public InstanceClass java_lang_Character() {
		return java_lang_Character;
	}

	@Override
	public InstanceClass java_lang_Short() {
		return java_lang_Short;
	}

	@Override
	public InstanceClass java_lang_Byte() {
		return java_lang_Byte;
	}

	@Override
	public InstanceClass java_lang_Boolean() {
		return java_lang_Boolean;
	}

	@Override
	public InstanceClass java_io_IOException() {
		return java_io_IOException;
	}

	@Override
	public InstanceClass java_lang_invoke_MethodType() {
		return java_lang_invoke_MethodType;
	}

	@Override
	public InstanceClass java_lang_reflect_AccessibleObject() {
		return java_lang_reflect_AccessibleObject;
	}

	@Override
	public InstanceClass java_security_PrivilegedExceptionAction() {
		return java_security_PrivilegedExceptionAction;
	}

	@Override
	public InstanceClass java_lang_invoke_MemberName() {
		return java_lang_invoke_MemberName;
	}

	@Override
	public InstanceClass java_lang_invoke_ResolvedMethodName() {
		return java_lang_invoke_ResolvedMethodName;
	}

	@Override
	public InstanceClass java_util_concurrent_atomic_AtomicLong() {
		return java_util_concurrent_atomic_AtomicLong;
	}

	@Override
	public InstanceClass java_io_FileDescriptor() {
		return java_io_FileDescriptor;
	}

	@Override
	public InstanceClass java_lang_ArrayStoreException() {
		return java_lang_ArrayStoreException;
	}

	@Override
	public InstanceClass java_util_zip_ZipFile() {
		return java_util_zip_ZipFile;
	}

	@Override
	public InstanceClass java_lang_IllegalMonitorStateException() {
		return java_lang_IllegalMonitorStateException;
	}

	@Override
	public InstanceClass sun_management_VMManagementImpl() {
		return sun_management_VMManagementImpl;
	}

	@Override
	public InstanceClass java_lang_Package() {
		return java_lang_Package;
	}

	@Override
	public InstanceClass java_lang_invoke_MethodHandle() {
		return java_lang_invoke_MethodHandle;
	}

	@Override
	public InstanceClass perf_Perf() {
		return perf_Perf;
	}

	@Override
	public InstanceClass java_nio_ByteBuffer() {
		return java_nio_ByteBuffer;
	}

	@Override
	public InstanceClass java_util_jar_JarFile() {
		return java_util_jar_JarFile;
	}

	@Override
	public InstanceClass java_lang_StrictMath() {
		return java_lang_StrictMath;
	}

	@Override
	public InstanceClass java_util_TimeZone() {
		return java_util_TimeZone;
	}

	@Override
	public InstanceClass java_util_zip_CRC32() {
		return java_util_zip_CRC32;
	}

	@Override
	public InstanceClass sun_security_provider_NativeSeedGenerator() {
		return sun_security_provider_NativeSeedGenerator;
	}

	@Override
	public InstanceClass java_net_NetworkInterface() {
		return java_net_NetworkInterface;
	}

	@Override
	public InstanceClass sun_security_provider_SeedGenerator() {
		return sun_security_provider_SeedGenerator;
	}

	@Override
	public InstanceClass java_lang_invoke_MethodHandles() {
		return java_lang_invoke_MethodHandles;
	}

	@Override
	public InstanceClass java_lang_invoke_MethodHandles$Lookup() {
		return java_lang_invoke_MethodHandles$Lookup;
	}

	@Override
	public InstanceClass reflect_ConstantPool() {
		return reflect_ConstantPool;
	}

	@Override
	public InstanceClass java_lang_reflect_Proxy() {
		return java_lang_reflect_Proxy;
	}

	@Override
	public InstanceClass java_util_zip_Inflater() {
		return java_util_zip_Inflater;
	}

	@Override
	public InstanceClass java_lang_invoke_CallSite() {
		return java_lang_invoke_CallSite;
	}

	@Override
	public InstanceClass java_lang_ProcessEnvironment() {
		return java_lang_ProcessEnvironment;
	}

	@Override
	public InstanceClass java_lang_InstantiationException() {
		return java_lang_InstantiationException;
	}

	@Override
	public InstanceClass reflect_MethodAccessorImpl() {
		return reflect_MethodAccessorImpl;
	}

	@Override
	public InstanceClass java_util_zip_ZipException() {
		return java_util_zip_ZipException;
	}

	@Override
	public InstanceClass java_lang_IllegalAccessException() {
		return java_lang_IllegalAccessException;
	}

	@Override
	public InstanceClass java_io_Serializable() {
		return java_io_Serializable;
	}

	@Override
	public InstanceClass java_lang_Cloneable() {
		return java_lang_Cloneable;
	}

	@Override
	public InstanceClass java_lang_IncompatibleClassChangeError() {
		return java_lang_IncompatibleClassChangeError;
	}

	@Override
	public InstanceClass java_io_FileNotFoundException() {
		return java_io_FileNotFoundException;
	}

	@Override
	public InstanceClass java_lang_InstantiationError() {
		return java_lang_InstantiationError;
	}

	@Override
	public InstanceClass internal_reflect_Reflection() {
		return internal_reflect_Reflection;
	}

	@Override
	public InstanceClass java_lang_ArithmeticException() {
		return java_lang_ArithmeticException;
	}

	@Override
	public InstanceClass java_io_FileOutputStream() {
		return java_io_FileOutputStream;
	}

	private static InstanceClass resolvedMemberName(VirtualMachine vm) {
		SafeClassLookup lookup = new SafeClassLookup(vm);
		InstanceClass jc = lookup.findBootstrapClassOrNull("java/lang/invoke/ResolvedMethodName");
		if (jc == null) {
			ClassWriter writer = new ClassWriter(0);
			writer.visit(Opcodes.V1_8, Modifier.ACC_VM_HIDDEN, "java/lang/invoke/ResolvedMethodName", null, "java/lang/Object", null);
			byte[] b = writer.toByteArray();
			ObjectValue nullValue = vm.getMemoryManager().nullValue();
			jc = vm.getOperations().defineClass(nullValue, "java/lang/invoke/ResolvedMethodName", b, 0, b.length, nullValue, "JVM_DefineClass");
		}
		return jc;
	}

	private static final class SafeClassLookup {
		final VirtualMachine vm;

		SafeClassLookup(VirtualMachine vm) {
			this.vm = vm;
		}

		InstanceClass findBootstrapClassOrNull(String... names) {
			for (String name : names) {
				JavaClass klass = vm.findBootstrapClass(name);
				if (klass != null) {
					return (InstanceClass) klass;
				}
			}
			return null;
		}

		InstanceClass findBootstrapClass(String... names) {
			InstanceClass klass = findBootstrapClassOrNull(names);
			if (klass != null) {
				return klass;
			}
			throw new PanicException("Class not found " + Arrays.toString(names));
		}
	}
}
