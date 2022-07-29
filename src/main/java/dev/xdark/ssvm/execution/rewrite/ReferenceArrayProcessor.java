package dev.xdark.ssvm.execution.rewrite;

import dev.xdark.ssvm.asm.VMTypeInsnNode;
import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.execution.Stack;

/**
 * Fast-path for NEWARRAY instruction.
 *
 * @author xDark
 */
public final class ReferenceArrayProcessor implements InstructionProcessor<VMTypeInsnNode> {

	@Override
	public Result execute(VMTypeInsnNode insn, ExecutionContext ctx) {
		Stack stack = ctx.getStack();
		int length = stack.popInt();
		stack.pushReference(ctx.getOperations().allocateArray(insn.getJavaType(), length));
		return Result.CONTINUE;
	}
}
