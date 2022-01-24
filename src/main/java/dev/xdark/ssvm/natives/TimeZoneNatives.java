package dev.xdark.ssvm.natives;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.execution.Result;
import lombok.experimental.UtilityClass;
import lombok.val;

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
		val vmi = vm.getInterface();
		val symbols = vm.getSymbols();
		val jc = symbols.java_util_TimeZone;
		vmi.setInvoker(jc, "getSystemTimeZoneID", "(Ljava/lang/String;)Ljava/lang/String;", ctx -> {
			val helper = vm.getHelper();
			ctx.setResult(helper.newUtf8(vm.getTimeZoneManager().getSystemTimeZoneId(helper.readUtf8(ctx.getLocals().load(0)))));
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "getSystemGMTOffsetID", "()Ljava/lang/String;", ctx -> {
			ctx.setResult(vm.getHelper().newUtf8(vm.getTimeZoneManager().getSystemGMTOffsetId()));
			return Result.ABORT;
		});
	}

}
