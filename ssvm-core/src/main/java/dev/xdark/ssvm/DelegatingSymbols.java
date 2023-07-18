package dev.xdark.ssvm;

import dev.xdark.ssvm.mirror.type.InstanceClass;
import dev.xdark.ssvm.symbol.Symbols;

/**
 * State-dependent VM symbols.
 *
 * @author xDark
 */
final class DelegatingSymbols implements Symbols {

	private Symbols symbols;

	@Override
	public InstanceClass java_lang_Object() {
		return symbols.java_lang_Object();
	}

	@Override
	public InstanceClass java_lang_Class() {
		return symbols.java_lang_Class();
	}

	@Override
	public InstanceClass java_lang_String() {
		return symbols.java_lang_String();
	}

	@Override
	public InstanceClass java_lang_ClassLoader() {
		return symbols.java_lang_ClassLoader();
	}

	@Override
	public InstanceClass java_lang_Thread() {
		return symbols.java_lang_Thread();
	}

	@Override
	public InstanceClass java_lang_ThreadGroup() {
		return symbols.java_lang_ThreadGroup();
	}

	@Override
	public InstanceClass java_lang_System() {
		return symbols.java_lang_System();
	}

	@Override
	public InstanceClass java_lang_Runtime() {
		return symbols.java_lang_Runtime();
	}

	@Override
	public InstanceClass java_lang_Throwable() {
		return symbols.java_lang_Throwable();
	}

	@Override
	public InstanceClass java_lang_Error() {
		return symbols.java_lang_Error();
	}

	@Override
	public InstanceClass java_lang_Exception() {
		return symbols.java_lang_Exception();
	}

	@Override
	public InstanceClass java_lang_NullPointerException() {
		return symbols.java_lang_NullPointerException();
	}

	@Override
	public InstanceClass java_lang_NoSuchFieldError() {
		return symbols.java_lang_NoSuchFieldError();
	}

	@Override
	public InstanceClass java_lang_NoSuchMethodError() {
		return symbols.java_lang_NoSuchMethodError();
	}

	@Override
	public InstanceClass java_lang_ArrayIndexOutOfBoundsException() {
		return symbols.java_lang_ArrayIndexOutOfBoundsException();
	}

	@Override
	public InstanceClass java_lang_ExceptionInInitializerError() {
		return symbols.java_lang_ExceptionInInitializerError();
	}

	@Override
	public InstanceClass java_lang_UnsatisfiedLinkError() {
		return symbols.java_lang_UnsatisfiedLinkError();
	}

	@Override
	public InstanceClass java_lang_InternalError() {
		return symbols.java_lang_InternalError();
	}

	@Override
	public InstanceClass java_lang_ClassCastException() {
		return symbols.java_lang_ClassCastException();
	}

	@Override
	public InstanceClass java_lang_invoke_MethodHandleNatives() {
		return symbols.java_lang_invoke_MethodHandleNatives();
	}

	@Override
	public InstanceClass java_lang_NoClassDefFoundError() {
		return symbols.java_lang_NoClassDefFoundError();
	}

	@Override
	public InstanceClass java_lang_ClassNotFoundException() {
		return symbols.java_lang_ClassNotFoundException();
	}

	@Override
	public InstanceClass java_util_Vector() {
		return symbols.java_util_Vector();
	}

	@Override
	public InstanceClass java_lang_OutOfMemoryError() {
		return symbols.java_lang_OutOfMemoryError();
	}

	@Override
	public InstanceClass java_lang_NegativeArraySizeException() {
		return symbols.java_lang_NegativeArraySizeException();
	}

	@Override
	public InstanceClass java_lang_IllegalArgumentException() {
		return symbols.java_lang_IllegalArgumentException();
	}

	@Override
	public InstanceClass java_lang_AbstractMethodError() {
		return symbols.java_lang_AbstractMethodError();
	}

	@Override
	public InstanceClass java_lang_reflect_Array() {
		return symbols.java_lang_reflect_Array();
	}

	@Override
	public InstanceClass java_lang_BootstrapMethodError() {
		return symbols.java_lang_BootstrapMethodError();
	}

	@Override
	public InstanceClass java_lang_IllegalStateException() {
		return symbols.java_lang_IllegalStateException();
	}

	@Override
	public InstanceClass java_lang_NoSuchMethodException() {
		return symbols.java_lang_NoSuchMethodException();
	}

	@Override
	public InstanceClass java_lang_InterruptedException() {
		return symbols.java_lang_InterruptedException();
	}

	@Override
	public InstanceClass java_lang_StackTraceElement() {
		return symbols.java_lang_StackTraceElement();
	}

	@Override
	public InstanceClass java_security_PrivilegedAction() {
		return symbols.java_security_PrivilegedAction();
	}

	@Override
	public InstanceClass reflect_ReflectionFactory() {
		return symbols.reflect_ReflectionFactory();
	}

	@Override
	public InstanceClass java_lang_reflect_Constructor() {
		return symbols.java_lang_reflect_Constructor();
	}

	@Override
	public InstanceClass java_lang_reflect_Method() {
		return symbols.java_lang_reflect_Method();
	}

	@Override
	public InstanceClass java_lang_reflect_Field() {
		return symbols.java_lang_reflect_Field();
	}

	@Override
	public InstanceClass java_lang_Long() {
		return symbols.java_lang_Long();
	}

	@Override
	public InstanceClass java_lang_Double() {
		return symbols.java_lang_Double();
	}

	@Override
	public InstanceClass java_lang_Integer() {
		return symbols.java_lang_Integer();
	}

	@Override
	public InstanceClass java_lang_Float() {
		return symbols.java_lang_Float();
	}

	@Override
	public InstanceClass java_lang_Character() {
		return symbols.java_lang_Character();
	}

	@Override
	public InstanceClass java_lang_Short() {
		return symbols.java_lang_Short();
	}

	@Override
	public InstanceClass java_lang_Byte() {
		return symbols.java_lang_Byte();
	}

	@Override
	public InstanceClass java_lang_Boolean() {
		return symbols.java_lang_Boolean();
	}

	@Override
	public InstanceClass java_io_IOException() {
		return symbols.java_io_IOException();
	}

	@Override
	public InstanceClass java_lang_invoke_MethodType() {
		return symbols.java_lang_invoke_MethodType();
	}

	@Override
	public InstanceClass java_lang_reflect_AccessibleObject() {
		return symbols.java_lang_reflect_AccessibleObject();
	}

	@Override
	public InstanceClass java_security_PrivilegedExceptionAction() {
		return symbols.java_security_PrivilegedExceptionAction();
	}

	@Override
	public InstanceClass java_lang_invoke_MemberName() {
		return symbols.java_lang_invoke_MemberName();
	}

	@Override
	public InstanceClass java_lang_invoke_ResolvedMethodName() {
		return symbols.java_lang_invoke_ResolvedMethodName();
	}

	@Override
	public InstanceClass java_util_concurrent_atomic_AtomicLong() {
		return symbols.java_util_concurrent_atomic_AtomicLong();
	}

	@Override
	public InstanceClass java_io_FileDescriptor() {
		return symbols.java_io_FileDescriptor();
	}

	@Override
	public InstanceClass java_lang_ArrayStoreException() {
		return symbols.java_lang_ArrayStoreException();
	}

	@Override
	public InstanceClass java_util_zip_ZipFile() {
		return symbols.java_util_zip_ZipFile();
	}

	@Override
	public InstanceClass java_lang_IllegalMonitorStateException() {
		return symbols.java_lang_IllegalMonitorStateException();
	}

	@Override
	public InstanceClass sun_management_VMManagementImpl() {
		return symbols.sun_management_VMManagementImpl();
	}

	@Override
	public InstanceClass java_lang_Package() {
		return symbols.java_lang_Package();
	}

	@Override
	public InstanceClass java_lang_invoke_MethodHandle() {
		return symbols.java_lang_invoke_MethodHandle();
	}

	@Override
	public InstanceClass perf_Perf() {
		return symbols.perf_Perf();
	}

	@Override
	public InstanceClass java_nio_ByteBuffer() {
		return symbols.java_nio_ByteBuffer();
	}

	@Override
	public InstanceClass java_nio_charset_StandardCharsets() {
		return symbols.java_nio_charset_StandardCharsets();
	}

	@Override
	public InstanceClass java_util_jar_JarFile() {
		return symbols.java_util_jar_JarFile();
	}

	@Override
	public InstanceClass java_lang_StrictMath() {
		return symbols.java_lang_StrictMath();
	}

	@Override
	public InstanceClass java_util_TimeZone() {
		return symbols.java_util_TimeZone();
	}

	@Override
	public InstanceClass java_util_zip_CRC32() {
		return symbols.java_util_zip_CRC32();
	}

	@Override
	public InstanceClass sun_security_provider_NativeSeedGenerator() {
		return symbols.sun_security_provider_NativeSeedGenerator();
	}

	@Override
	public InstanceClass java_net_NetworkInterface() {
		return symbols.java_net_NetworkInterface();
	}

	@Override
	public InstanceClass sun_security_provider_SeedGenerator() {
		return symbols.sun_security_provider_SeedGenerator();
	}

	@Override
	public InstanceClass java_lang_invoke_MethodHandles() {
		return symbols.java_lang_invoke_MethodHandles();
	}

	@Override
	public InstanceClass java_lang_invoke_MethodHandles$Lookup() {
		return symbols.java_lang_invoke_MethodHandles$Lookup();
	}

	@Override
	public InstanceClass reflect_ConstantPool() {
		return symbols.reflect_ConstantPool();
	}

	@Override
	public InstanceClass java_lang_reflect_Proxy() {
		return symbols.java_lang_reflect_Proxy();
	}

	@Override
	public InstanceClass java_util_zip_Inflater() {
		return symbols.java_util_zip_Inflater();
	}

	@Override
	public InstanceClass java_lang_invoke_CallSite() {
		return symbols.java_lang_invoke_CallSite();
	}

	@Override
	public InstanceClass java_lang_ProcessEnvironment() {
		return symbols.java_lang_ProcessEnvironment();
	}

	@Override
	public InstanceClass java_lang_InstantiationException() {
		return symbols.java_lang_InstantiationException();
	}

	@Override
	public InstanceClass reflect_MethodAccessorImpl() {
		return symbols.reflect_MethodAccessorImpl();
	}

	@Override
	public InstanceClass java_util_zip_ZipException() {
		return symbols.java_util_zip_ZipException();
	}

	@Override
	public InstanceClass java_lang_IllegalAccessException() {
		return symbols.java_lang_IllegalAccessException();
	}

	@Override
	public InstanceClass java_io_Serializable() {
		return symbols.java_io_Serializable();
	}

	@Override
	public InstanceClass java_lang_Cloneable() {
		return symbols.java_lang_Cloneable();
	}

	@Override
	public InstanceClass java_lang_IncompatibleClassChangeError() {
		return symbols.java_lang_IncompatibleClassChangeError();
	}

	@Override
	public InstanceClass java_io_FileNotFoundException() {
		return symbols.java_io_FileNotFoundException();
	}

	@Override
	public InstanceClass java_lang_InstantiationError() {
		return symbols.java_lang_InstantiationError();
	}

	@Override
	public InstanceClass internal_reflect_Reflection() {
		return symbols.internal_reflect_Reflection();
	}

	@Override
	public InstanceClass java_lang_ArithmeticException() {
		return symbols.java_lang_ArithmeticException();
	}

	@Override
	public InstanceClass java_io_FileOutputStream() {
		return symbols.java_io_FileOutputStream();
	}

	/**
	 * @param symbols New symbols.
	 */
	void setSymbols(Symbols symbols) {
		this.symbols = symbols;
	}
}
