package dev.xdark.ssvm.natives;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.api.VMInterface;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.mirror.type.InstanceClass;
import dev.xdark.ssvm.operation.VMOperations;
import dev.xdark.ssvm.symbol.Symbols;
import lombok.experimental.UtilityClass;

/**
 * Initializes java/util/TimeZone.
 *
 * @author xDark
 */
@UtilityClass
public class TimeZoneNatives {

	/**
	 * @param vm VM instance.
	 */
	public void init(VirtualMachine vm) {
		VMInterface vmi = vm.getInterface();
		Symbols symbols = vm.getSymbols();
		InstanceClass jc = symbols.java_util_TimeZone();
		vmi.setInvoker(jc, "getSystemTimeZoneID", "(Ljava/lang/String;)Ljava/lang/String;", ctx -> {
			VMOperations ops = vm.getOperations();
			ctx.setResult(ops.newUtf8(vm.getTimeManager().getSystemTimeZoneId(ops.readUtf8(ctx.getLocals().loadReference(0)))));
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "getSystemGMTOffsetID", "()Ljava/lang/String;", ctx -> {
			ctx.setResult(vm.getOperations().newUtf8(vm.getTimeManager().getSystemGMTOffsetId()));
			return Result.ABORT;
		});
	}

}
