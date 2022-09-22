package dev.xdark.ssvm.natives;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.api.VMInterface;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.mirror.type.InstanceJavaClass;
import dev.xdark.ssvm.symbol.VMSymbols;
import lombok.experimental.UtilityClass;

/**
 * Initializes java/util/concurrent/AtomicLong.
 *
 * @author xDark
 */
@UtilityClass
public class AtomicLongNatives {

	/**
	 * @param vm VM instance.
	 */
	public void init(VirtualMachine vm) {
		VMInterface vmi = vm.getInterface();
		VMSymbols symbols = vm.getSymbols();
		InstanceJavaClass jc = symbols.java_util_concurrent_atomic_AtomicLong();
		vmi.setInvoker(jc, "VMSupportsCS8", "()Z", ctx -> {
			ctx.setResult(0);
			return Result.ABORT;
		});
	}
}
