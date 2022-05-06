package dev.xdark.ssvm.symbol;

import dev.xdark.ssvm.mirror.InstanceJavaClass;

/**
 * Common VM symbols.
 *
 * @author xDark
 */
public interface VMSymbols {

	InstanceJavaClass java_lang_Object();
	InstanceJavaClass java_lang_Class();
	InstanceJavaClass java_lang_String();
	InstanceJavaClass java_lang_ClassLoader();
	InstanceJavaClass java_lang_Thread();
	InstanceJavaClass java_lang_ThreadGroup();
	InstanceJavaClass java_lang_System();
	InstanceJavaClass java_lang_Throwable();
	InstanceJavaClass java_lang_Error();
	InstanceJavaClass java_lang_Exception();
	InstanceJavaClass java_lang_NullPointerException();
	InstanceJavaClass java_lang_NoSuchFieldError();
	InstanceJavaClass java_lang_NoSuchMethodError();
	InstanceJavaClass java_lang_ArrayIndexOutOfBoundsException();
	InstanceJavaClass java_lang_ExceptionInInitializerError();
	InstanceJavaClass java_lang_UnsatisfiedLinkError();
	InstanceJavaClass java_lang_InternalError();
	InstanceJavaClass java_lang_ClassCastException();
	InstanceJavaClass java_lang_invoke_MethodHandleNatives();
	InstanceJavaClass java_lang_NoClassDefFoundError();
	InstanceJavaClass java_lang_ClassNotFoundException();
	InstanceJavaClass java_util_Vector();
	InstanceJavaClass java_lang_OutOfMemoryError();
	InstanceJavaClass java_lang_NegativeArraySizeException();
	InstanceJavaClass java_lang_IllegalArgumentException();
	InstanceJavaClass java_lang_AbstractMethodError();
	InstanceJavaClass java_lang_reflect_Array();
	InstanceJavaClass java_lang_BootstrapMethodError();
	InstanceJavaClass java_lang_IllegalStateException();
	InstanceJavaClass java_lang_NoSuchMethodException();
	InstanceJavaClass java_lang_InterruptedException();
	InstanceJavaClass java_lang_StackTraceElement();
	InstanceJavaClass java_security_PrivilegedAction();
	InstanceJavaClass reflect_ReflectionFactory();
	InstanceJavaClass java_lang_reflect_Constructor();
	InstanceJavaClass java_lang_reflect_Method();
	InstanceJavaClass java_lang_reflect_Field();
	InstanceJavaClass java_lang_Long();
	InstanceJavaClass java_lang_Double();
	InstanceJavaClass java_lang_Integer();
	InstanceJavaClass java_lang_Float();
	InstanceJavaClass java_lang_Character();
	InstanceJavaClass java_lang_Short();
	InstanceJavaClass java_lang_Byte();
	InstanceJavaClass java_lang_Boolean();
	InstanceJavaClass java_io_IOException();
	InstanceJavaClass java_lang_invoke_MethodType();
	InstanceJavaClass java_lang_reflect_AccessibleObject();
	InstanceJavaClass java_security_PrivilegedExceptionAction();
	InstanceJavaClass java_lang_invoke_MemberName();
	InstanceJavaClass java_lang_invoke_ResolvedMethodName();
	InstanceJavaClass java_util_concurrent_atomic_AtomicLong();
	InstanceJavaClass java_io_FileDescriptor();
	InstanceJavaClass java_lang_ArrayStoreException();
	InstanceJavaClass java_util_zip_ZipFile();
	InstanceJavaClass java_lang_IllegalMonitorStateException();
	InstanceJavaClass sun_management_VMManagementImpl();
	InstanceJavaClass java_lang_Package();
	InstanceJavaClass java_lang_invoke_MethodHandle();
	InstanceJavaClass perf_Perf();
	InstanceJavaClass java_nio_ByteBuffer();
	InstanceJavaClass java_util_jar_JarFile();
	InstanceJavaClass java_lang_StrictMath();
	InstanceJavaClass java_util_TimeZone();
	InstanceJavaClass java_util_zip_CRC32();
	InstanceJavaClass sun_security_provider_NativeSeedGenerator();
	InstanceJavaClass java_net_NetworkInterface();
	InstanceJavaClass sun_security_provider_SeedGenerator();
	InstanceJavaClass java_lang_invoke_MethodHandles();
	InstanceJavaClass java_lang_invoke_MethodHandles$Lookup();
	InstanceJavaClass reflect_ConstantPool();
	InstanceJavaClass java_lang_reflect_Proxy();
	InstanceJavaClass java_util_zip_Inflater();
	InstanceJavaClass java_lang_invoke_CallSite();
	InstanceJavaClass java_lang_ProcessEnvironment();
	InstanceJavaClass java_lang_InstantiationException();
	InstanceJavaClass reflect_MethodAccessorImpl();
	InstanceJavaClass java_util_zip_ZipException();
	InstanceJavaClass java_lang_IllegalAccessException();
	InstanceJavaClass java_lang_Module();
	InstanceJavaClass java_io_Serializable();
	InstanceJavaClass java_lang_Cloneable();
}
