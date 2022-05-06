package dev.xdark.ssvm.natives;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.api.VMInterface;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.util.VMSymbols;
import dev.xdark.ssvm.value.DoubleValue;
import dev.xdark.ssvm.value.LongValue;
import lombok.experimental.UtilityClass;

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
		VMInterface vmi = vm.getInterface();
		VMSymbols symbols = vm.getSymbols();
		InstanceJavaClass jc = symbols.java_lang_Double;
		vmi.setInvoker(jc, "doubleToRawLongBits", "(D)J", ctx -> {
			ctx.setResult(LongValue.of(Double.doubleToRawLongBits(ctx.getLocals().load(0).asDouble())));
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "longBitsToDouble", "(J)D", ctx -> {
			ctx.setResult(new DoubleValue(Double.longBitsToDouble(ctx.getLocals().load(0).asLong())));
			return Result.ABORT;
		});
	}
}
