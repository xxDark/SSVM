package dev.xdark.ssvm.natives;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.api.MethodInvoker;
import dev.xdark.ssvm.api.VMInterface;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.mirror.type.InstanceJavaClass;
import lombok.experimental.UtilityClass;

/**
 * Initializes misc/Signal.
 *
 * @author xDark
 */
@UtilityClass
public class SignalNatives {

	/**
	 * @param vm VM instance.
	 */
	public void init(VirtualMachine vm) {
		InstanceJavaClass signal = (InstanceJavaClass) vm.findBootstrapClass("jdk/internal/misc/Signal");
		if (signal == null) {
			signal = (InstanceJavaClass) vm.findBootstrapClass("sun/misc/Signal");
			if (signal == null) {
				throw new IllegalStateException("Unable to locate Signal class");
			}
		}
		// TODO: implement this?
		VMInterface vmi = vm.getInterface();
		MethodInvoker findSignal = ctx -> {
			ctx.setResult(0);
			return Result.ABORT;
		};
		if (!vmi.setInvoker(signal, "findSignal0", "(Ljava/lang/String;)I", findSignal)) {
			if (!vmi.setInvoker(signal, "findSignal", "(Ljava/lang/String;)I", findSignal)) {
				throw new IllegalStateException("Could not locate Signal#findSignal");
			}
		}
		vmi.setInvoker(signal, "handle0", "(IJ)J", ctx -> {
			ctx.setResult(0L);
			return Result.ABORT;
		});
	}
}
