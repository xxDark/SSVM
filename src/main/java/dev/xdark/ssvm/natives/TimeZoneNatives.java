package dev.xdark.ssvm.natives;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.api.VMInterface;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.util.VMHelper;
import dev.xdark.ssvm.symbol.VMSymbols;
import lombok.experimental.UtilityClass;

/**
 * Initializes java/util/TimeZone.
 *
 * @author xDark
 */
@UtilityClass
public class TimeZoneNatives {

	/**
	 * @param vm
	 * 		VM instance.
	 */
	public void init(VirtualMachine vm) {
		VMInterface vmi = vm.getInterface();
		VMSymbols symbols = vm.getSymbols();
		InstanceJavaClass jc = symbols.java_util_TimeZone();
		vmi.setInvoker(jc, "getSystemTimeZoneID", "(Ljava/lang/String;)Ljava/lang/String;", ctx -> {
			VMHelper helper = vm.getHelper();
			ctx.setResult(helper.newUtf8(vm.getTimeManager().getSystemTimeZoneId(helper.readUtf8(ctx.getLocals().load(0)))));
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "getSystemGMTOffsetID", "()Ljava/lang/String;", ctx -> {
			ctx.setResult(vm.getHelper().newUtf8(vm.getTimeManager().getSystemGMTOffsetId()));
			return Result.ABORT;
		});
	}

}
