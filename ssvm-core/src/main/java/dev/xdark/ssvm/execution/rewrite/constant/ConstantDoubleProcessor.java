package dev.xdark.ssvm.execution.rewrite.constant;

import dev.xdark.ssvm.asm.ConstantDoubleInsnNode;
import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;

/**
 * Pushes constant onto the stack.
 *
 * @author xDark
 */
public final class ConstantDoubleProcessor implements InstructionProcessor<ConstantDoubleInsnNode> {

	@Override
	public Result execute(ConstantDoubleInsnNode insn, ExecutionContext<?> ctx) {
		ctx.getStack().pushDouble(insn.getValue());
		return Result.CONTINUE;
	}
}
