package dev.xdark.ssvm.symbol;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.mirror.InstanceJavaClass;

/**
 * Implementation of VM symbols that
 * always throws exception due to uninitialized VM.
 * 
 * @author xDark
 */
public final class UninitializedVMSymbols implements VMSymbols {

	private final VirtualMachine vm;

	public UninitializedVMSymbols(VirtualMachine vm) {
		this.vm = vm;
	}

	// java/lang/Object and java/lang/Class are special cases
	// when we want them to be accessible even when VM symbols
	// are not yet initialized
	@Override
	public InstanceJavaClass java_lang_Object() {
		return (InstanceJavaClass) vm.findBootstrapClass("java/lang/Object");
	}

	@Override
	public InstanceJavaClass java_lang_Class() {
		return (InstanceJavaClass) vm.findBootstrapClass("java/lang/Class");
	}

	@Override
	public InstanceJavaClass java_lang_String() {
		return uninitialized();
	}

	@Override
	public InstanceJavaClass java_lang_ClassLoader() {
		return uninitialized();
	}

	@Override
	public InstanceJavaClass java_lang_Thread() {
		return uninitialized();
	}

	@Override
	public InstanceJavaClass java_lang_ThreadGroup() {
		return uninitialized();
	}

	@Override
	public InstanceJavaClass java_lang_System() {
		return uninitialized();
	}

	@Override
	public InstanceJavaClass java_lang_Throwable() {
		return uninitialized();
	}

	@Override
	public InstanceJavaClass java_lang_Error() {
		return uninitialized();
	}

	@Override
	public InstanceJavaClass java_lang_Exception() {
		return uninitialized();
	}

	@Override
	public InstanceJavaClass java_lang_NullPointerException() {
		return uninitialized();
	}

	@Override
	public InstanceJavaClass java_lang_NoSuchFieldError() {
		return uninitialized();
	}

	@Override
	public InstanceJavaClass java_lang_NoSuchMethodError() {
		return uninitialized();
	}

	@Override
	public InstanceJavaClass java_lang_ArrayIndexOutOfBoundsException() {
		return uninitialized();
	}

	@Override
	public InstanceJavaClass java_lang_ExceptionInInitializerError() {
		return uninitialized();
	}

	@Override
	public InstanceJavaClass java_lang_UnsatisfiedLinkError() {
		return uninitialized();
	}

	@Override
	public InstanceJavaClass java_lang_InternalError() {
		return uninitialized();
	}

	@Override
	public InstanceJavaClass java_lang_ClassCastException() {
		return uninitialized();
	}

	@Override
	public InstanceJavaClass java_lang_invoke_MethodHandleNatives() {
		return uninitialized();
	}

	@Override
	public InstanceJavaClass java_lang_NoClassDefFoundError() {
		return uninitialized();
	}

	@Override
	public InstanceJavaClass java_lang_ClassNotFoundException() {
		return uninitialized();
	}

	@Override
	public InstanceJavaClass java_util_Vector() {
		return uninitialized();
	}

	@Override
	public InstanceJavaClass java_lang_OutOfMemoryError() {
		return uninitialized();
	}

	@Override
	public InstanceJavaClass java_lang_NegativeArraySizeException() {
		return uninitialized();
	}

	@Override
	public InstanceJavaClass java_lang_IllegalArgumentException() {
		return uninitialized();
	}

	@Override
	public InstanceJavaClass java_lang_AbstractMethodError() {
		return uninitialized();
	}

	@Override
	public InstanceJavaClass java_lang_reflect_Array() {
		return uninitialized();
	}

	@Override
	public InstanceJavaClass java_lang_BootstrapMethodError() {
		return uninitialized();
	}

	@Override
	public InstanceJavaClass java_lang_IllegalStateException() {
		return uninitialized();
	}

	@Override
	public InstanceJavaClass java_lang_NoSuchMethodException() {
		return uninitialized();
	}

	@Override
	public InstanceJavaClass java_lang_InterruptedException() {
		return uninitialized();
	}

	@Override
	public InstanceJavaClass java_lang_StackTraceElement() {
		return uninitialized();
	}

	@Override
	public InstanceJavaClass java_security_PrivilegedAction() {
		return uninitialized();
	}

	@Override
	public InstanceJavaClass reflect_ReflectionFactory() {
		return uninitialized();
	}

	@Override
	public InstanceJavaClass java_lang_reflect_Constructor() {
		return uninitialized();
	}

	@Override
	public InstanceJavaClass java_lang_reflect_Method() {
		return uninitialized();
	}

	@Override
	public InstanceJavaClass java_lang_reflect_Field() {
		return uninitialized();
	}

	@Override
	public InstanceJavaClass java_lang_Long() {
		return uninitialized();
	}

	@Override
	public InstanceJavaClass java_lang_Double() {
		return uninitialized();
	}

	@Override
	public InstanceJavaClass java_lang_Integer() {
		return uninitialized();
	}

	@Override
	public InstanceJavaClass java_lang_Float() {
		return uninitialized();
	}

	@Override
	public InstanceJavaClass java_lang_Character() {
		return uninitialized();
	}

	@Override
	public InstanceJavaClass java_lang_Short() {
		return uninitialized();
	}

	@Override
	public InstanceJavaClass java_lang_Byte() {
		return uninitialized();
	}

	@Override
	public InstanceJavaClass java_lang_Boolean() {
		return uninitialized();
	}

	@Override
	public InstanceJavaClass java_io_IOException() {
		return uninitialized();
	}

	@Override
	public InstanceJavaClass java_lang_invoke_MethodType() {
		return uninitialized();
	}

	@Override
	public InstanceJavaClass java_lang_reflect_AccessibleObject() {
		return uninitialized();
	}

	@Override
	public InstanceJavaClass java_security_PrivilegedExceptionAction() {
		return uninitialized();
	}

	@Override
	public InstanceJavaClass java_lang_invoke_MemberName() {
		return uninitialized();
	}

	@Override
	public InstanceJavaClass java_lang_invoke_ResolvedMethodName() {
		return uninitialized();
	}

	@Override
	public InstanceJavaClass java_util_concurrent_atomic_AtomicLong() {
		return uninitialized();
	}

	@Override
	public InstanceJavaClass java_io_FileDescriptor() {
		return uninitialized();
	}

	@Override
	public InstanceJavaClass java_lang_ArrayStoreException() {
		return uninitialized();
	}

	@Override
	public InstanceJavaClass java_util_zip_ZipFile() {
		return uninitialized();
	}

	@Override
	public InstanceJavaClass java_lang_IllegalMonitorStateException() {
		return uninitialized();
	}

	@Override
	public InstanceJavaClass sun_management_VMManagementImpl() {
		return uninitialized();
	}

	@Override
	public InstanceJavaClass java_lang_Package() {
		return uninitialized();
	}

	@Override
	public InstanceJavaClass java_lang_invoke_MethodHandle() {
		return uninitialized();
	}

	@Override
	public InstanceJavaClass perf_Perf() {
		return uninitialized();
	}

	@Override
	public InstanceJavaClass java_nio_ByteBuffer() {
		return uninitialized();
	}

	@Override
	public InstanceJavaClass java_util_jar_JarFile() {
		return uninitialized();
	}

	@Override
	public InstanceJavaClass java_lang_StrictMath() {
		return uninitialized();
	}

	@Override
	public InstanceJavaClass java_util_TimeZone() {
		return uninitialized();
	}

	@Override
	public InstanceJavaClass java_util_zip_CRC32() {
		return uninitialized();
	}

	@Override
	public InstanceJavaClass sun_security_provider_NativeSeedGenerator() {
		return uninitialized();
	}

	@Override
	public InstanceJavaClass java_net_NetworkInterface() {
		return uninitialized();
	}

	@Override
	public InstanceJavaClass sun_security_provider_SeedGenerator() {
		return uninitialized();
	}

	@Override
	public InstanceJavaClass java_lang_invoke_MethodHandles() {
		return uninitialized();
	}

	@Override
	public InstanceJavaClass java_lang_invoke_MethodHandles$Lookup() {
		return uninitialized();
	}

	@Override
	public InstanceJavaClass reflect_ConstantPool() {
		return uninitialized();
	}

	@Override
	public InstanceJavaClass java_lang_reflect_Proxy() {
		return uninitialized();
	}

	@Override
	public InstanceJavaClass java_util_zip_Inflater() {
		return uninitialized();
	}

	@Override
	public InstanceJavaClass java_lang_invoke_CallSite() {
		return uninitialized();
	}

	@Override
	public InstanceJavaClass java_lang_ProcessEnvironment() {
		return uninitialized();
	}

	@Override
	public InstanceJavaClass java_lang_InstantiationException() {
		return uninitialized();
	}

	@Override
	public InstanceJavaClass reflect_MethodAccessorImpl() {
		return uninitialized();
	}

	@Override
	public InstanceJavaClass java_util_zip_ZipException() {
		return uninitialized();
	}

	@Override
	public InstanceJavaClass java_lang_IllegalAccessException() {
		return uninitialized();
	}

	@Override
	public InstanceJavaClass java_lang_Module() {
		return uninitialized();
	}

	@Override
	public InstanceJavaClass java_io_Serializable() {
		return uninitialized();
	}

	@Override
	public InstanceJavaClass java_lang_Cloneable() {
		return uninitialized();
	}

	@Override
	public InstanceJavaClass java_lang_IncompatibleClassChangeError() {
		return uninitialized();
	}

	@Override
	public InstanceJavaClass java_io_FileNotFoundException() {
		return uninitialized();
	}

	@Override
	public InstanceJavaClass java_lang_InstantiationError() {
		return uninitialized();
	}

	private static InstanceJavaClass uninitialized() {
		throw new IllegalStateException("VM is not initialized!");
	}
}
