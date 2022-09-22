package dev.xdark.ssvm.natives;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.api.MethodInvoker;
import dev.xdark.ssvm.api.VMInterface;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.mirror.type.InstanceJavaClass;
import lombok.experimental.UtilityClass;

/**
 * Initializes jdk/internal/misc/CDS.
 *
 * @author xDark
 */
@UtilityClass
public class CDSNatives {

	/**
	 * @param vm VM instance.
	 */
	public void init(VirtualMachine vm) {
		InstanceJavaClass jc = (InstanceJavaClass) vm.findBootstrapClass("jdk/internal/misc/CDS");
		if (jc != null) {
			VMInterface vmi = vm.getInterface();
			MethodInvoker stub = ctx -> {
				ctx.setResult(0);
				return Result.ABORT;
			};
			vmi.setInvoker(jc, "isDumpingClassList0", "()Z", stub);
			vmi.setInvoker(jc, "isDumpingArchive0", "()Z", stub);
			vmi.setInvoker(jc, "isSharingEnabled0", "()Z", stub);
			vmi.setInvoker(jc, "getRandomSeedForDumping", "()J", ctx -> {
				ctx.setResult(0L);
				return Result.ABORT;
			});
			vmi.setInvoker(jc, "initializeFromArchive", "(Ljava/lang/Class;)V", MethodInvoker.noop());
		}
	}
}
