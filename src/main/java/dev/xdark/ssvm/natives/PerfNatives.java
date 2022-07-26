package dev.xdark.ssvm.natives;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.api.MethodInvoker;
import dev.xdark.ssvm.api.VMInterface;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.symbol.VMSymbols;
import dev.xdark.ssvm.value.IntValue;
import dev.xdark.ssvm.value.Value;
import lombok.experimental.UtilityClass;

/**
 * Initializes Perf.
 *
 * @author xDark
 */
@UtilityClass
public class PerfNatives {

	/**
	 * @param vm VM instance.
	 */
	public void init(VirtualMachine vm) {
		VMInterface vmi = vm.getInterface();
		VMSymbols symbols = vm.getSymbols();
		InstanceJavaClass jc = symbols.perf_Perf();
		vmi.setInvoker(jc, "registerNatives", "()V", MethodInvoker.noop());
		vmi.setInvoker(jc, "createLong", "(Ljava/lang/String;IIJ)Ljava/nio/ByteBuffer;", ctx -> {
			Value buf = vm.getHelper().invokeStatic(symbols.java_nio_ByteBuffer(), "allocateDirect", "(I)Ljava/nio/ByteBuffer;", new Value[]{IntValue.of(8)}).getResult();
			ctx.setResult(buf);
			return Result.ABORT;
		});
	}
}
