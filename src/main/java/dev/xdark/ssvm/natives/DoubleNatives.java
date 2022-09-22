package dev.xdark.ssvm.natives;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.api.VMInterface;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.mirror.type.InstanceJavaClass;
import dev.xdark.ssvm.symbol.VMSymbols;
import lombok.experimental.UtilityClass;

/**
 * Initializes java/lang/Double.
 *
 * @author xDark
 */
@UtilityClass
public class DoubleNatives {

	/**
	 * @param vm VM instance.
	 */
	public void init(VirtualMachine vm) {
		VMInterface vmi = vm.getInterface();
		VMSymbols symbols = vm.getSymbols();
		InstanceJavaClass jc = symbols.java_lang_Double();
		vmi.setInvoker(jc, "doubleToRawLongBits", "(D)J", ctx -> {
			ctx.setResult(Double.doubleToRawLongBits(ctx.getLocals().loadDouble(0)));
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "longBitsToDouble", "(J)D", ctx -> {
			ctx.setResult(Double.longBitsToDouble(ctx.getLocals().loadLong(0)));
			return Result.ABORT;
		});
	}
}
