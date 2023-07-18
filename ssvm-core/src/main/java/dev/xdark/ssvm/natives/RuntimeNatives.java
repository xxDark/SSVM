package dev.xdark.ssvm.natives;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.api.MethodInvoker;
import dev.xdark.ssvm.api.VMInterface;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.memory.allocation.MemoryAllocator;
import dev.xdark.ssvm.memory.allocation.MemoryAllocatorStatistics;
import dev.xdark.ssvm.mirror.type.InstanceClass;
import lombok.experimental.UtilityClass;

/**
 * Initializes java/lang/Runtime.
 *
 * @author xDark
 */
@UtilityClass
public class RuntimeNatives {

	/**
	 * @param vm VM instance.
	 */
	public void init(VirtualMachine vm) {
		VMInterface vmi = vm.getInterface();
		InstanceClass runtime = vm.getSymbols().java_lang_Runtime();
		vmi.setInvoker(runtime, "availableProcessors", "()I", ctx -> {
			ctx.setResult(Runtime.getRuntime().availableProcessors());
			return Result.ABORT;
		});
		MemoryAllocator memoryAllocator = vm.getMemoryAllocator();
		MemoryAllocatorStatistics statistics = memoryAllocator.dumpStatistics();
		vmi.setInvoker(runtime, "freeMemory", "()J", ctx -> {
			ctx.setResult(statistics == null ? 0L : statistics.freeSpace());
			return Result.ABORT;
		});
		vmi.setInvoker(runtime, "totalMemory", "()J", ctx -> {
			ctx.setResult(statistics == null ? 0L : statistics.totalSpace());
			return Result.ABORT;
		});
		vmi.setInvoker(runtime, "maxMemory", "()J", ctx -> {
			ctx.setResult(statistics == null ? 0L : statistics.maxSpace());
			return Result.ABORT;
		});
		vmi.setInvoker(runtime, "gc", "()V", MethodInvoker.noop());
	}
}
