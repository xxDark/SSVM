package dev.xdark.ssvm.natives;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.mirror.JavaClass;
import dev.xdark.ssvm.value.ArrayValue;
import dev.xdark.ssvm.value.IntValue;
import dev.xdark.ssvm.value.JavaValue;
import lombok.experimental.UtilityClass;
import lombok.val;

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
		val vmi = vm.getInterface();
		val symbols = vm.getSymbols();
		val array = symbols.java_lang_reflect_Array;
		vmi.setInvoker(array, "getLength", "(Ljava/lang/Object;)I", ctx -> {
			val value = ctx.getLocals().load(0);
			vm.getHelper().checkArray(value);
			ctx.setResult(new IntValue(((ArrayValue) value).getLength()));
			return Result.ABORT;
		});
		vmi.setInvoker(array, "newArray", "(Ljava/lang/Class;I)Ljava/lang/Object;", ctx -> {
			val locals = ctx.getLocals();
			val local = locals.load(0);
			val helper = vm.getHelper();
			helper.checkNotNull(local);
			if (!(local instanceof JavaValue)) {
				helper.throwException(symbols.java_lang_IllegalArgumentException);
			}
			val wrapper = ((JavaValue<?>) local).getValue();
			if (!(wrapper instanceof JavaClass)) {
				helper.throwException(symbols.java_lang_IllegalArgumentException);
			}
			val klass = (JavaClass) wrapper;
			if (klass.isArray()) {
				helper.throwException(symbols.java_lang_IllegalArgumentException);
			}
			val length = locals.load(1).asInt();
			val result = helper.newArray(klass, length);
			ctx.setResult(result);
			return Result.ABORT;
		});
	}
}
