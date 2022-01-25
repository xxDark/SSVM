package dev.xdark.ssvm.natives;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.value.IntValue;
import lombok.experimental.UtilityClass;
import lombok.val;

/**
 * Initializes java/lang/Runtime.
 *
 * @author xDark
 */
@UtilityClass
public class RuntimeNatives {

	/**
	 * @param vm
	 * 		VM instance.
	 */
	public void init(VirtualMachine vm) {
		val vmi = vm.getInterface();
		val runtime = (InstanceJavaClass) vm.findBootstrapClass("java/lang/Runtime");
		vmi.setInvoker(runtime, "availableProcessors", "()I", ctx -> {
			ctx.setResult(IntValue.of(Runtime.getRuntime().availableProcessors()));
			return Result.ABORT;
		});
	}
}
