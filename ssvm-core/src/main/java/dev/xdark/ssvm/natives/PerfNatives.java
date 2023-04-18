package dev.xdark.ssvm.natives;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.api.MethodInvoker;
import dev.xdark.ssvm.api.VMInterface;
import dev.xdark.ssvm.execution.Locals;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.mirror.type.InstanceClass;
import dev.xdark.ssvm.mirror.member.JavaMethod;
import dev.xdark.ssvm.symbol.Symbols;
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
		Symbols symbols = vm.getSymbols();
		InstanceClass jc = symbols.perf_Perf();
		vmi.setInvoker(jc, "registerNatives", "()V", MethodInvoker.noop());
		vmi.setInvoker(jc, "createLong", "(Ljava/lang/String;IIJ)Ljava/nio/ByteBuffer;", ctx -> {
			JavaMethod allocateDirect = vm.getLinkResolver().resolveStaticMethod(symbols.java_nio_ByteBuffer(), "allocateDirect", "(I)Ljava/nio/ByteBuffer;");
			Locals locals = vm.getThreadStorage().newLocals(allocateDirect);
			locals.setInt(0, 8);
			vm.getOperations().invoke(allocateDirect, locals, ctx.returnSink());
			return Result.ABORT;
		});
	}
}
