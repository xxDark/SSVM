package dev.xdark.ssvm;

import dev.xdark.jlinker.Resolution;
import dev.xdark.jlinker.ResolutionError;
import dev.xdark.jlinker.Result;
import dev.xdark.ssvm.mirror.member.JavaField;
import dev.xdark.ssvm.mirror.member.JavaMethod;
import dev.xdark.ssvm.mirror.type.JavaClass;
import dev.xdark.ssvm.operation.VMOperations;
import dev.xdark.ssvm.symbol.Symbols;

class LinkHelper {

	private final VirtualMachine vm;

	LinkHelper(VirtualMachine vm) {
		this.vm = vm;
	}

	void checkMethod(JavaClass type, String name, String desc, Result<Resolution<JavaClass, JavaMethod>> result) {
		if (!result.isSuccess()) {
			ResolutionError error = result.error();
			VMOperations ops = vm.getOperations();
			Symbols symbols = vm.getSymbols();
			switch (error) {
				case METHOD_IS_ABSTRACT:
					ops.throwException(symbols.java_lang_AbstractMethodError(), format(type, name, desc));
					break;
				case NO_SUCH_METHOD:
					ops.throwException(symbols.java_lang_NoSuchMethodError(), format(type, name, desc));
					break;
				default:
					ops.throwException(symbols.java_lang_IncompatibleClassChangeError());
			}
		}
	}

	void checkField(JavaClass type, String name, String desc, Result<Resolution<JavaClass, JavaField>> result) {
		if (!result.isSuccess()) {
			if (result.error() == ResolutionError.NO_SUCH_FIELD) {
				vm.getOperations().throwException(vm.getSymbols().java_lang_NoSuchMethodError(), format(type, name, desc));
			} else {
				vm.getOperations().throwException(vm.getSymbols().java_lang_IncompatibleClassChangeError());
			}
		}
	}

	private static String format(JavaClass type, String name, String desc) {
		return type.getInternalName() + '.' + name + ' ' + desc;
	}
}
