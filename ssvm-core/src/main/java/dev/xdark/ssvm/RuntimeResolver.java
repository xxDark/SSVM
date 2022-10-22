package dev.xdark.ssvm;

import dev.xdark.jlinker.Resolution;
import dev.xdark.jlinker.Result;
import dev.xdark.ssvm.mirror.member.JavaMethod;
import dev.xdark.ssvm.mirror.type.JavaClass;
import dev.xdark.ssvm.value.ObjectValue;

/**
 * Runtime resolver wrapper around jlinker.
 *
 * @author xDark
 */
public final class RuntimeResolver {

	private final VirtualMachine vm;
	private final LinkHelper linkHelper;
	private final dev.xdark.jlinker.RuntimeResolver<JavaClass, JavaMethod> delegate;

	RuntimeResolver(VirtualMachine vm, LinkResolver resolver) {
		this.vm = vm;
		linkHelper = new LinkHelper(vm);
		delegate = dev.xdark.jlinker.RuntimeResolver.jvm(resolver.delegate);
	}

	public JavaMethod resolveVirtualMethod(ObjectValue value, String name, String descriptor) {
		if (value.isNull()) {
			vm.getOperations().throwException(vm.getSymbols().java_lang_NullPointerException());
		}
		JavaClass type = value.getJavaClass();
		Result<Resolution<JavaClass, JavaMethod>> result = delegate.resolveVirtualMethod(type.linkerInfo(), name, descriptor);
		linkHelper.checkMethod(type, name, descriptor, result);
		return result.value().member().innerValue();
	}

	public JavaMethod resolveInterfaceMethod(ObjectValue value, String name, String descriptor) {
		if (value.isNull()) {
			vm.getOperations().throwException(vm.getSymbols().java_lang_NullPointerException());
		}
		JavaClass type = value.getJavaClass();
		Result<Resolution<JavaClass, JavaMethod>> result = delegate.resolveInterfaceMethod(type.linkerInfo(), name, descriptor);
		linkHelper.checkMethod(type, name, descriptor, result);
		return result.value().member().innerValue();
	}
}
