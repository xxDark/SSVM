package dev.xdark.ssvm.natives;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.api.MethodInvoker;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.value.IntValue;
import dev.xdark.ssvm.value.Value;
import lombok.experimental.UtilityClass;
import lombok.val;

/**
 * Initializes Perf.
 *
 * @author xDark
 */
@UtilityClass
public class PerfNatives {

	/**
	 * @param vm
	 * 		VM instance.
	 */
	public void init(VirtualMachine vm) {
		val vmi = vm.getInterface();
		val symbols = vm.getSymbols();
		val jc = symbols.perf_Perf;
		vmi.setInvoker(jc, "registerNatives", "()V", MethodInvoker.noop());
		vmi.setInvoker(jc, "createLong", "(Ljava/lang/String;IIJ)Ljava/nio/ByteBuffer;", ctx -> {
			val buf = vm.getHelper().invokeStatic(symbols.java_nio_ByteBuffer, "allocateDirect", "(I)Ljava/nio/ByteBuffer;", new Value[0], new Value[]{new IntValue(8)}).getResult();
			ctx.setResult(buf);
			return Result.ABORT;
		});
	}
}
