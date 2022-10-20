package dev.xdark.ssvm.execution.rewrite;

import dev.xdark.ssvm.asm.ConstantLongInsnNode;
import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;

/**
 * Pushes constant onto the stack.
 *
 * @author xDark
 */
public final class ConstantLongProcessor implements InstructionProcessor<ConstantLongInsnNode> {

	@Override
	public Result execute(ConstantLongInsnNode insn, ExecutionContext<?> ctx) {
		ctx.getStack().pushLong(insn.getValue());
		return Result.CONTINUE;
	}
}
