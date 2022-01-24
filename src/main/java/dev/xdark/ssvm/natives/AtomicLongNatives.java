package dev.xdark.ssvm.natives;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.value.IntValue;
import lombok.experimental.UtilityClass;
import lombok.val;

/**
 * Initializes java/util/concurrent/AtomicLong.
 *
 * @author xDark
 */
@UtilityClass
public class AtomicLongNatives {

	/**
	 * @param vm
	 * 		VM instance.
	 */
	public void init(VirtualMachine vm) {
		val vmi = vm.getInterface();
		val symbols = vm.getSymbols();
		val atomicLong = symbols.java_util_concurrent_atomic_AtomicLong;
		vmi.setInvoker(atomicLong, "VMSupportsCS8", "()Z", ctx -> {
			ctx.setResult(IntValue.ZERO);
			return Result.ABORT;
		});
	}
}
