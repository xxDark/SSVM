package dev.xdark.ssvm;

import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.symbol.VMSymbols;

/**
 * State-dependent VM symbols.
 *
 * @author xDark
 */
final class DelegatingVMSymbols implements VMSymbols {

	private VMSymbols symbols;

	@Override
	public InstanceJavaClass java_lang_Object() {
		return symbols.java_lang_Object();
	}

	@Override
	public InstanceJavaClass java_lang_Class() {
		return symbols.java_lang_Class();
	}

	@Override
	public InstanceJavaClass java_lang_String() {
		return symbols.java_lang_String();
	}

	@Override
	public InstanceJavaClass java_lang_ClassLoader() {
		return symbols.java_lang_ClassLoader();
	}

	@Override
	public InstanceJavaClass java_lang_Thread() {
		return symbols.java_lang_Thread();
	}

	@Override
	public InstanceJavaClass java_lang_ThreadGroup() {
		return symbols.java_lang_ThreadGroup();
	}

	@Override
	public InstanceJavaClass java_lang_System() {
		return symbols.java_lang_System();
	}

	@Override
	public InstanceJavaClass java_lang_Throwable() {
		return symbols.java_lang_Throwable();
	}

	@Override
	public InstanceJavaClass java_lang_Error() {
		return symbols.java_lang_Error();
	}

	@Override
	public InstanceJavaClass java_lang_Exception() {
		return symbols.java_lang_Exception();
	}

	@Override
	public InstanceJavaClass java_lang_NullPointerException() {
		return symbols.java_lang_NullPointerException();
	}

	@Override
	public InstanceJavaClass java_lang_NoSuchFieldError() {
		return symbols.java_lang_NoSuchFieldError();
	}

	@Override
	public InstanceJavaClass java_lang_NoSuchMethodError() {
		return symbols.java_lang_NoSuchMethodError();
	}

	@Override
	public InstanceJavaClass java_lang_ArrayIndexOutOfBoundsException() {
		return symbols.java_lang_ArrayIndexOutOfBoundsException();
	}

	@Override
	public InstanceJavaClass java_lang_ExceptionInInitializerError() {
		return symbols.java_lang_ExceptionInInitializerError();
	}

	@Override
	public InstanceJavaClass java_lang_UnsatisfiedLinkError() {
		return symbols.java_lang_UnsatisfiedLinkError();
	}

	@Override
	public InstanceJavaClass java_lang_InternalError() {
		return symbols.java_lang_InternalError();
	}

	@Override
	public InstanceJavaClass java_lang_ClassCastException() {
		return symbols.java_lang_ClassCastException();
	}

	@Override
	public InstanceJavaClass java_lang_invoke_MethodHandleNatives() {
		return symbols.java_lang_invoke_MethodHandleNatives();
	}

	@Override
	public InstanceJavaClass java_lang_NoClassDefFoundError() {
		return symbols.java_lang_NoClassDefFoundError();
	}

	@Override
	public InstanceJavaClass java_lang_ClassNotFoundException() {
		return symbols.java_lang_ClassNotFoundException();
	}

	@Override
	public InstanceJavaClass java_util_Vector() {
		return symbols.java_util_Vector();
	}

	@Override
	public InstanceJavaClass java_lang_OutOfMemoryError() {
		return symbols.java_lang_OutOfMemoryError();
	}

	@Override
	public InstanceJavaClass java_lang_NegativeArraySizeException() {
		return symbols.java_lang_NegativeArraySizeException();
	}

	@Override
	public InstanceJavaClass java_lang_IllegalArgumentException() {
		return symbols.java_lang_IllegalArgumentException();
	}

	@Override
	public InstanceJavaClass java_lang_AbstractMethodError() {
		return symbols.java_lang_AbstractMethodError();
	}

	@Override
	public InstanceJavaClass java_lang_reflect_Array() {
		return symbols.java_lang_reflect_Array();
	}

	@Override
	public InstanceJavaClass java_lang_BootstrapMethodError() {
		return symbols.java_lang_BootstrapMethodError();
	}

	@Override
	public InstanceJavaClass java_lang_IllegalStateException() {
		return symbols.java_lang_IllegalStateException();
	}

	@Override
	public InstanceJavaClass java_lang_NoSuchMethodException() {
		return symbols.java_lang_NoSuchMethodException();
	}

	@Override
	public InstanceJavaClass java_lang_InterruptedException() {
		return symbols.java_lang_InterruptedException();
	}

	@Override
	public InstanceJavaClass java_lang_StackTraceElement() {
		return symbols.java_lang_StackTraceElement();
	}

	@Override
	public InstanceJavaClass java_security_PrivilegedAction() {
		return symbols.java_security_PrivilegedAction();
	}

	@Override
	public InstanceJavaClass reflect_ReflectionFactory() {
		return symbols.reflect_ReflectionFactory();
	}

	@Override
	public InstanceJavaClass java_lang_reflect_Constructor() {
		return symbols.java_lang_reflect_Constructor();
	}

	@Override
	public InstanceJavaClass java_lang_reflect_Method() {
		return symbols.java_lang_reflect_Method();
	}

	@Override
	public InstanceJavaClass java_lang_reflect_Field() {
		return symbols.java_lang_reflect_Field();
	}

	@Override
	public InstanceJavaClass java_lang_Long() {
		return symbols.java_lang_Long();
	}

	@Override
	public InstanceJavaClass java_lang_Double() {
		return symbols.java_lang_Double();
	}

	@Override
	public InstanceJavaClass java_lang_Integer() {
		return symbols.java_lang_Integer();
	}

	@Override
	public InstanceJavaClass java_lang_Float() {
		return symbols.java_lang_Float();
	}

	@Override
	public InstanceJavaClass java_lang_Character() {
		return symbols.java_lang_Character();
	}

	@Override
	public InstanceJavaClass java_lang_Short() {
		return symbols.java_lang_Short();
	}

	@Override
	public InstanceJavaClass java_lang_Byte() {
		return symbols.java_lang_Byte();
	}

	@Override
	public InstanceJavaClass java_lang_Boolean() {
		return symbols.java_lang_Boolean();
	}

	@Override
	public InstanceJavaClass java_io_IOException() {
		return symbols.java_io_IOException();
	}

	@Override
	public InstanceJavaClass java_lang_invoke_MethodType() {
		return symbols.java_lang_invoke_MethodType();
	}

	@Override
	public InstanceJavaClass java_lang_reflect_AccessibleObject() {
		return symbols.java_lang_reflect_AccessibleObject();
	}

	@Override
	public InstanceJavaClass java_security_PrivilegedExceptionAction() {
		return symbols.java_security_PrivilegedExceptionAction();
	}

	@Override
	public InstanceJavaClass java_lang_invoke_MemberName() {
		return symbols.java_lang_invoke_MemberName();
	}

	@Override
	public InstanceJavaClass java_lang_invoke_ResolvedMethodName() {
		return symbols.java_lang_invoke_ResolvedMethodName();
	}

	@Override
	public InstanceJavaClass java_util_concurrent_atomic_AtomicLong() {
		return symbols.java_util_concurrent_atomic_AtomicLong();
	}

	@Override
	public InstanceJavaClass java_io_FileDescriptor() {
		return symbols.java_io_FileDescriptor();
	}

	@Override
	public InstanceJavaClass java_lang_ArrayStoreException() {
		return symbols.java_lang_ArrayStoreException();
	}

	@Override
	public InstanceJavaClass java_util_zip_ZipFile() {
		return symbols.java_util_zip_ZipFile();
	}

	@Override
	public InstanceJavaClass java_lang_IllegalMonitorStateException() {
		return symbols.java_lang_IllegalMonitorStateException();
	}

	@Override
	public InstanceJavaClass sun_management_VMManagementImpl() {
		return symbols.sun_management_VMManagementImpl();
	}

	@Override
	public InstanceJavaClass java_lang_Package() {
		return symbols.java_lang_Package();
	}

	@Override
	public InstanceJavaClass java_lang_invoke_MethodHandle() {
		return symbols.java_lang_invoke_MethodHandle();
	}

	@Override
	public InstanceJavaClass perf_Perf() {
		return symbols.perf_Perf();
	}

	@Override
	public InstanceJavaClass java_nio_ByteBuffer() {
		return symbols.java_nio_ByteBuffer();
	}

	@Override
	public InstanceJavaClass java_util_jar_JarFile() {
		return symbols.java_util_jar_JarFile();
	}

	@Override
	public InstanceJavaClass java_lang_StrictMath() {
		return symbols.java_lang_StrictMath();
	}

	@Override
	public InstanceJavaClass java_util_TimeZone() {
		return symbols.java_util_TimeZone();
	}

	@Override
	public InstanceJavaClass java_util_zip_CRC32() {
		return symbols.java_util_zip_CRC32();
	}

	@Override
	public InstanceJavaClass sun_security_provider_NativeSeedGenerator() {
		return symbols.sun_security_provider_NativeSeedGenerator();
	}

	@Override
	public InstanceJavaClass java_net_NetworkInterface() {
		return symbols.java_net_NetworkInterface();
	}

	@Override
	public InstanceJavaClass sun_security_provider_SeedGenerator() {
		return symbols.sun_security_provider_SeedGenerator();
	}

	@Override
	public InstanceJavaClass java_lang_invoke_MethodHandles() {
		return symbols.java_lang_invoke_MethodHandles();
	}

	@Override
	public InstanceJavaClass java_lang_invoke_MethodHandles$Lookup() {
		return symbols.java_lang_invoke_MethodHandles$Lookup();
	}

	@Override
	public InstanceJavaClass reflect_ConstantPool() {
		return symbols.reflect_ConstantPool();
	}

	@Override
	public InstanceJavaClass java_lang_reflect_Proxy() {
		return symbols.java_lang_reflect_Proxy();
	}

	@Override
	public InstanceJavaClass java_util_zip_Inflater() {
		return symbols.java_util_zip_Inflater();
	}

	@Override
	public InstanceJavaClass java_lang_invoke_CallSite() {
		return symbols.java_lang_invoke_CallSite();
	}

	@Override
	public InstanceJavaClass java_lang_ProcessEnvironment() {
		return symbols.java_lang_ProcessEnvironment();
	}

	@Override
	public InstanceJavaClass java_lang_InstantiationException() {
		return symbols.java_lang_InstantiationException();
	}

	@Override
	public InstanceJavaClass reflect_MethodAccessorImpl() {
		return symbols.reflect_MethodAccessorImpl();
	}

	@Override
	public InstanceJavaClass java_util_zip_ZipException() {
		return symbols.java_util_zip_ZipException();
	}

	@Override
	public InstanceJavaClass java_lang_IllegalAccessException() {
		return symbols.java_lang_IllegalAccessException();
	}

	@Override
	public InstanceJavaClass java_lang_Module() {
		return symbols.java_lang_Module();
	}

	@Override
	public InstanceJavaClass java_io_Serializable() {
		return symbols.java_io_Serializable();
	}

	@Override
	public InstanceJavaClass java_lang_Cloneable() {
		return symbols.java_lang_Cloneable();
	}

	/**
	 * @param symbols
	 * 		New symbols.
	 */
	void setSymbols(VMSymbols symbols) {
		this.symbols = symbols;
	}
}
