package dev.xdark.ssvm.execution.rewrite;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.asm.VMCallInsnNode;
import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Locals;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.execution.Stack;
import dev.xdark.ssvm.mirror.member.JavaMethod;
import dev.xdark.ssvm.thread.ThreadStorage;

/**
 * Base class for VM calls.
 *
 * @author xDark
 */
abstract class AbstractVMCallProcessor implements InstructionProcessor<VMCallInsnNode> {

	@Override
	public final Result execute(VMCallInsnNode insn, ExecutionContext<?> ctx) {
		JavaMethod method = insn.getResolved();
		if (method == null) {
			// The one who override the method
			// must replace resolved field, if needed
			method = resolveMethod(insn, ctx);
		}
		method.getOwner().initialize();
		VirtualMachine vm = ctx.getVM();
		Stack callerStack = ctx.getStack();
		ThreadStorage storage = vm.getThreadStorage();
		int maxArgs = method.getMaxArgs();
		Locals locals = storage.newLocals(method);
		callerStack.sinkInto(locals, maxArgs);
		vm.getHelper().invoke(method, locals, callerStack);
		return Result.CONTINUE;
	}

	protected abstract JavaMethod resolveMethod(VMCallInsnNode insn, ExecutionContext<?> ctx);
}
