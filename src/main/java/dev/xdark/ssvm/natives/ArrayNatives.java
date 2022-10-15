package dev.xdark.ssvm.natives;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.api.VMInterface;
import dev.xdark.ssvm.execution.Locals;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.mirror.type.InstanceClass;
import dev.xdark.ssvm.mirror.type.JavaClass;
import dev.xdark.ssvm.operation.VMOperations;
import dev.xdark.ssvm.symbol.Symbols;
import dev.xdark.ssvm.value.ArrayValue;
import dev.xdark.ssvm.value.InstanceValue;
import dev.xdark.ssvm.value.ObjectValue;
import lombok.experimental.UtilityClass;

/**
 * Initializes java/lang/reflect/Array.
 *
 * @author xDark
 */
@UtilityClass
public class ArrayNatives {

	/**
	 * @param vm VM instance.
	 */
	public void init(VirtualMachine vm) {
		VMInterface vmi = vm.getInterface();
		Symbols symbols = vm.getSymbols();
		InstanceClass array = symbols.java_lang_reflect_Array();
		vmi.setInvoker(array, "getLength", "(Ljava/lang/Object;)I", ctx -> {
			ObjectValue value = ctx.getLocals().loadReference(0);
			vm.getOperations().checkNotNull(value);
			ctx.setResult(((ArrayValue) value).getLength());
			return Result.ABORT;
		});
		vmi.setInvoker(array, "newArray", "(Ljava/lang/Class;I)Ljava/lang/Object;", ctx -> {
			Locals locals = ctx.getLocals();
			VMOperations ops = vm.getOperations();
			InstanceValue classType = ops.checkNotNull(locals.loadReference(0));
			JavaClass klass = vm.getClassStorage().lookup(classType);
			int length = locals.loadInt(1);
			ArrayValue result = ops.allocateArray(klass, length);
			ctx.setResult(result);
			return Result.ABORT;
		});
	}
}
