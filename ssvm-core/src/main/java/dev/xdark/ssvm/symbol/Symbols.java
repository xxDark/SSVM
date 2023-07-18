package dev.xdark.ssvm.symbol;

import dev.xdark.ssvm.mirror.type.InstanceClass;

/**
 * Common VM symbols.
 *
 * @author xDark
 */
public interface Symbols {

	InstanceClass java_lang_Object();

	InstanceClass java_lang_Class();

	InstanceClass java_lang_String();

	InstanceClass java_lang_ClassLoader();

	InstanceClass java_lang_Thread();

	InstanceClass java_lang_ThreadGroup();

	InstanceClass java_lang_System();

	InstanceClass java_lang_Runtime();

	InstanceClass java_lang_Throwable();

	InstanceClass java_lang_Error();

	InstanceClass java_lang_Exception();

	InstanceClass java_lang_NullPointerException();

	InstanceClass java_lang_NoSuchFieldError();

	InstanceClass java_lang_NoSuchMethodError();

	InstanceClass java_lang_ArrayIndexOutOfBoundsException();

	InstanceClass java_lang_ExceptionInInitializerError();

	InstanceClass java_lang_UnsatisfiedLinkError();

	InstanceClass java_lang_InternalError();

	InstanceClass java_lang_ClassCastException();

	InstanceClass java_lang_invoke_MethodHandleNatives();

	InstanceClass java_lang_NoClassDefFoundError();

	InstanceClass java_lang_ClassNotFoundException();

	InstanceClass java_util_Vector();

	InstanceClass java_lang_OutOfMemoryError();

	InstanceClass java_lang_NegativeArraySizeException();

	InstanceClass java_lang_IllegalArgumentException();

	InstanceClass java_lang_AbstractMethodError();

	InstanceClass java_lang_reflect_Array();

	InstanceClass java_lang_BootstrapMethodError();

	InstanceClass java_lang_IllegalStateException();

	InstanceClass java_lang_NoSuchMethodException();

	InstanceClass java_lang_InterruptedException();

	InstanceClass java_lang_StackTraceElement();

	InstanceClass java_security_PrivilegedAction();

	InstanceClass reflect_ReflectionFactory();

	InstanceClass java_lang_reflect_Constructor();

	InstanceClass java_lang_reflect_Method();

	InstanceClass java_lang_reflect_Field();

	InstanceClass java_lang_Long();

	InstanceClass java_lang_Double();

	InstanceClass java_lang_Integer();

	InstanceClass java_lang_Float();

	InstanceClass java_lang_Character();

	InstanceClass java_lang_Short();

	InstanceClass java_lang_Byte();

	InstanceClass java_lang_Boolean();

	InstanceClass java_io_IOException();

	InstanceClass java_lang_invoke_MethodType();

	InstanceClass java_lang_reflect_AccessibleObject();

	InstanceClass java_security_PrivilegedExceptionAction();

	InstanceClass java_lang_invoke_MemberName();

	InstanceClass java_lang_invoke_ResolvedMethodName();

	InstanceClass java_util_concurrent_atomic_AtomicLong();

	InstanceClass java_io_FileDescriptor();

	InstanceClass java_lang_ArrayStoreException();

	InstanceClass java_util_zip_ZipFile();

	InstanceClass java_lang_IllegalMonitorStateException();

	InstanceClass sun_management_VMManagementImpl();

	InstanceClass java_lang_Package();

	InstanceClass java_lang_invoke_MethodHandle();

	InstanceClass perf_Perf();

	InstanceClass java_nio_ByteBuffer();

	InstanceClass java_nio_charset_StandardCharsets();

	InstanceClass java_util_jar_JarFile();

	InstanceClass java_lang_StrictMath();

	InstanceClass java_util_TimeZone();

	InstanceClass java_util_zip_CRC32();

	InstanceClass sun_security_provider_NativeSeedGenerator();

	InstanceClass java_net_NetworkInterface();

	InstanceClass sun_security_provider_SeedGenerator();

	InstanceClass java_lang_invoke_MethodHandles();

	InstanceClass java_lang_invoke_MethodHandles$Lookup();

	InstanceClass reflect_ConstantPool();

	InstanceClass java_lang_reflect_Proxy();

	InstanceClass java_util_zip_Inflater();

	InstanceClass java_lang_invoke_CallSite();

	InstanceClass java_lang_ProcessEnvironment();

	InstanceClass java_lang_InstantiationException();

	InstanceClass reflect_MethodAccessorImpl();

	InstanceClass java_util_zip_ZipException();

	InstanceClass java_lang_IllegalAccessException();

	InstanceClass java_io_Serializable();

	InstanceClass java_lang_Cloneable();

	InstanceClass java_lang_IncompatibleClassChangeError();

	InstanceClass java_io_FileNotFoundException();

	InstanceClass java_lang_InstantiationError();

	InstanceClass internal_reflect_Reflection();

	InstanceClass java_lang_ArithmeticException();

	InstanceClass java_io_FileOutputStream();
}
