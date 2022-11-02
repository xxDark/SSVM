package dev.xdark.ssvm;

import dev.xdark.jlinker.Resolution;
import dev.xdark.jlinker.Result;
import dev.xdark.ssvm.mirror.member.JavaField;
import dev.xdark.ssvm.mirror.member.JavaMethod;
import dev.xdark.ssvm.mirror.type.JavaClass;

/**
 * Link resolver wrapper around jlinker.
 *
 * @author xDark
 */
public final class LinkResolver {

	private final VirtualMachine vm;
	private final LinkHelper linkHelper;
	final dev.xdark.jlinker.LinkResolver<JavaClass, JavaMethod, JavaField> delegate;

	/**
	 * @param vm VM instance.
	 */
	public LinkResolver(VirtualMachine vm) {
		this.vm = vm;
		linkHelper = new LinkHelper(vm);
		delegate = dev.xdark.jlinker.LinkResolver.jvm(new ImmediateArenaAllocator<>());
	}

	public JavaMethod resolveStaticMethod(JavaClass type, String name, String desc, boolean itf) {
		Result<Resolution<JavaClass, JavaMethod>> result = delegate.resolveStaticMethod(type.linkerInfo(), name, desc, itf);
		linkHelper.checkMethod(type, name, desc, result);
		return result.value().member().innerValue();
	}

	public JavaMethod resolveStaticMethod(JavaClass type, String name, String desc) {
		return resolveStaticMethod(type, name, desc, type.isInterface());
	}

	public JavaMethod resolveVirtualMethod(JavaClass type, String name, String desc) {
		Result<Resolution<JavaClass, JavaMethod>> result = delegate.resolveVirtualMethod(type.linkerInfo(), name, desc);
		linkHelper.checkMethod(type, name, desc, result);
		return result.value().member().innerValue();
	}

	public JavaMethod resolveInterfaceMethod(JavaClass type, String name, String desc) {
		Result<Resolution<JavaClass, JavaMethod>> result = delegate.resolveInterfaceMethod(type.linkerInfo(), name, desc);
		linkHelper.checkMethod(type, name, desc, result);
		return result.value().member().innerValue();
	}

	public JavaField resolveStaticField(JavaClass type, String name, String desc) {
		Result<Resolution<JavaClass, JavaField>> result = delegate.resolveStaticField(type.linkerInfo(), name, desc);
		linkHelper.checkField(type, name, desc, result);
		return result.value().member().innerValue();
	}

	public JavaField resolveVirtualField(JavaClass type, String name, String desc) {
		Result<Resolution<JavaClass, JavaField>> result = delegate.resolveVirtualField(type.linkerInfo(), name, desc);
		linkHelper.checkField(type, name, desc, result);
		return result.value().member().innerValue();
	}
}
