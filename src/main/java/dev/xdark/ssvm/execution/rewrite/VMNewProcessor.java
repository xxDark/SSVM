package dev.xdark.ssvm.execution.rewrite;

import dev.xdark.ssvm.asm.VMTypeInsnNode;
import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;

/**
 * Fast path for NEW instruction.
 *
 * @author xDark
 */
public final class VMNewProcessor implements InstructionProcessor<VMTypeInsnNode> {

	@Override
	public Result execute(VMTypeInsnNode insn, ExecutionContext<?> ctx) {
		ctx.getStack().pushReference(ctx.getOperations().allocateInstance(insn.getJavaType()));
		ctx.pollSafePointAndSuspend();
		return Result.CONTINUE;
	}
}
