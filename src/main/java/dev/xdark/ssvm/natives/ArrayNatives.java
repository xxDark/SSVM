package dev.xdark.ssvm.natives;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.api.VMInterface;
import dev.xdark.ssvm.execution.Locals;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.mirror.JavaClass;
import dev.xdark.ssvm.util.VMHelper;
import dev.xdark.ssvm.util.VMSymbols;
import dev.xdark.ssvm.value.ArrayValue;
import dev.xdark.ssvm.value.IntValue;
import dev.xdark.ssvm.value.JavaValue;
import dev.xdark.ssvm.value.Value;
import lombok.experimental.UtilityClass;

/**
 * Initializes java/lang/reflect/Array.
 *
 * @author xDark
 */
@UtilityClass
public class ArrayNatives {

	/**
	 * @param vm
	 * 		VM instance.
	 */
	public void init(VirtualMachine vm) {
		VMInterface vmi = vm.getInterface();
		VMSymbols symbols = vm.getSymbols();
		InstanceJavaClass array = symbols.java_lang_reflect_Array;
		vmi.setInvoker(array, "getLength", "(Ljava/lang/Object;)I", ctx -> {
			Value value = ctx.getLocals().load(0);
			vm.getHelper().checkArray(value);
			ctx.setResult(IntValue.of(((ArrayValue) value).getLength()));
			return Result.ABORT;
		});
		vmi.setInvoker(array, "newArray", "(Ljava/lang/Class;I)Ljava/lang/Object;", ctx -> {
			Locals locals = ctx.getLocals();
			Value local = locals.load(0);
			VMHelper helper = vm.getHelper();
			helper.checkNotNull(local);
			if (!(local instanceof JavaValue)) {
				helper.throwException(symbols.java_lang_IllegalArgumentException);
			}
			Object wrapper = ((JavaValue<?>) local).getValue();
			if (!(wrapper instanceof JavaClass)) {
				helper.throwException(symbols.java_lang_IllegalArgumentException);
			}
			JavaClass klass = (JavaClass) wrapper;
			if (klass.isArray()) {
				helper.throwException(symbols.java_lang_IllegalArgumentException);
			}
			int length = locals.load(1).asInt();
			ArrayValue result = helper.newArray(klass, length);
			ctx.setResult(result);
			return Result.ABORT;
		});
	}
}
