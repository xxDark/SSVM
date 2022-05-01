package dev.xdark.ssvm.natives;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.api.MethodInvoker;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.value.IntValue;
import dev.xdark.ssvm.value.LongValue;
import lombok.experimental.UtilityClass;
import lombok.val;

/**
 * Initializes jdk/internal/misc/CDS.
 *
 * @author xDark
 */
@UtilityClass
public class CDSNatives {

	/**
	 * @param vm
	 * 		VM instance.
	 */
	public void init(VirtualMachine vm) {
		val jc = (InstanceJavaClass) vm.findBootstrapClass("jdk/internal/misc/CDS");
		if (jc != null) {
			val vmi = vm.getInterface();
			val stub = (MethodInvoker) ctx -> {
				ctx.setResult(IntValue.ZERO);
				return Result.ABORT;
			};
			vmi.setInvoker(jc, "isDumpingClassList0", "()Z",stub);
			vmi.setInvoker(jc, "isDumpingArchive0", "()Z", stub);
			vmi.setInvoker(jc, "isSharingEnabled0", "()Z", stub);
			vmi.setInvoker(jc, "getRandomSeedForDumping", "()J", ctx -> {
				ctx.setResult(LongValue.ZERO);
				return Result.ABORT;
			});
			vmi.setInvoker(jc, "initializeFromArchive", "(Ljava/lang/Class;)V", MethodInvoker.noop());
		}
	}
}
