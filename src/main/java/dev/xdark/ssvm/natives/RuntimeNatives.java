package dev.xdark.ssvm.natives;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.value.IntValue;
import dev.xdark.ssvm.value.LongValue;
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
		val memoryManager = vm.getMemoryManager();
		vmi.setInvoker(runtime, "freeMemory", "()J", ctx -> {
			ctx.setResult(LongValue.of(memoryManager.freeMemory()));
			return Result.ABORT;
		});
		vmi.setInvoker(runtime, "totalMemory", "()J", ctx -> {
			ctx.setResult(LongValue.of(memoryManager.totalMemory()));
			return Result.ABORT;
		});
		vmi.setInvoker(runtime, "maxMemory", "()J", ctx -> {
			ctx.setResult(LongValue.of(memoryManager.maxMemory()));
			return Result.ABORT;
		});
	}
}
