package dev.xdark.ssvm.natives;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.value.DoubleValue;
import dev.xdark.ssvm.value.LongValue;
import lombok.experimental.UtilityClass;
import lombok.val;

/**
 * Initializes java/lang/Double.
 *
 * @author xDark
 */
@UtilityClass
public class DoubleNatives {

	/**
	 * @param vm
	 * 		VM instance.
	 */
	public void init(VirtualMachine vm) {
		val vmi = vm.getInterface();
		val symbols = vm.getSymbols();
		val doubleClass = symbols.java_lang_Double;
		vmi.setInvoker(doubleClass, "doubleToRawLongBits", "(D)J", ctx -> {
			ctx.setResult(new LongValue(Double.doubleToRawLongBits(ctx.getLocals().load(0).asDouble())));
			return Result.ABORT;
		});
		vmi.setInvoker(doubleClass, "longBitsToDouble", "(J)D", ctx -> {
			ctx.setResult(new DoubleValue(Double.longBitsToDouble(ctx.getLocals().load(0).asLong())));
			return Result.ABORT;
		});
	}
}
