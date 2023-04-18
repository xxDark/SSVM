package dev.xdark.ssvm.execution.rewrite.constant;

import dev.xdark.ssvm.asm.ConstantReferenceInsnNode;
import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;

/**
 * Pushes constant onto the stack.
 *
 * @author xDark
 */
public final class ConstantReferenceProcessor implements InstructionProcessor<ConstantReferenceInsnNode> {

	@Override
	public Result execute(ConstantReferenceInsnNode insn, ExecutionContext<?> ctx) {
		ctx.getStack().pushReference(insn.getValue());
		return Result.CONTINUE;
	}
}
