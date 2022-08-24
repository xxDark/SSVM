package dev.xdark.ssvm.execution.rewrite;

import dev.xdark.ssvm.asm.ConstantFloatInsnNode;
import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;

/**
 * Pushes constant onto the stack.
 *
 * @author xDark
 */
public final class ConstantFloatProcessor implements InstructionProcessor<ConstantFloatInsnNode> {

	@Override
	public Result execute(ConstantFloatInsnNode insn, ExecutionContext ctx) {
		ctx.getStack().pushFloat(insn.getValue());
		return Result.CONTINUE;
	}
}
