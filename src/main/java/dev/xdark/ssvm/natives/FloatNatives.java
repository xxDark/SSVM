package dev.xdark.ssvm.natives;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.value.FloatValue;
import dev.xdark.ssvm.value.IntValue;
import lombok.experimental.UtilityClass;
import lombok.val;

/**
 * Initializes java/lang/Float.
 *
 * @author xDark
 */
@UtilityClass
public class FloatNatives {

	/**
	 * Initializes java/lang/Float.
	 *
	 * @param vm
	 * 		VM instance.
	 */
	public void init(VirtualMachine vm) {
		val vmi = vm.getInterface();
		val symbols = vm.getSymbols();
		val floatClass = symbols.java_lang_Float;
		vmi.setInvoker(floatClass, "floatToRawIntBits", "(F)I", ctx -> {
			ctx.setResult(IntValue.of(Float.floatToRawIntBits(ctx.getLocals().load(0).asFloat())));
			return Result.ABORT;
		});
		vmi.setInvoker(floatClass, "intBitsToFloat", "(I)F", ctx -> {
			ctx.setResult(new FloatValue(Float.intBitsToFloat(ctx.getLocals().load(0).asInt())));
			return Result.ABORT;
		});
	}

}
