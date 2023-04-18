package dev.xdark.ssvm.natives;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.api.VMInterface;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.mirror.type.InstanceClass;
import dev.xdark.ssvm.symbol.Symbols;
import lombok.experimental.UtilityClass;

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
	 * @param vm VM instance.
	 */
	public void init(VirtualMachine vm) {
		VMInterface vmi = vm.getInterface();
		Symbols symbols = vm.getSymbols();
		InstanceClass jc = symbols.java_lang_Float();
		vmi.setInvoker(jc, "floatToRawIntBits", "(F)I", ctx -> {
			ctx.setResult(Float.floatToRawIntBits(ctx.getLocals().loadFloat(0)));
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "intBitsToFloat", "(I)F", ctx -> {
			ctx.setResult(Float.intBitsToFloat(ctx.getLocals().loadInt(0)));
			return Result.ABORT;
		});
	}

}
