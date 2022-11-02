package dev.xdark.ssvm.execution.rewrite.constant;

import dev.xdark.ssvm.asm.ConstantIntInsnNode;
import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;

/**
 * Pushes constant onto the stack.
 *
 * @author xDark
 */
public final class ConstantIntProcessor implements InstructionProcessor<ConstantIntInsnNode> {

	@Override
	public Result execute(ConstantIntInsnNode insn, ExecutionContext<?> ctx) {
		ctx.getStack().pushInt(insn.getValue());
		return Result.CONTINUE;
	}
}
