package dev.xdark.ssvm.execution.asm;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;
import org.objectweb.asm.tree.IntInsnNode;

/**
 * Pushes short onto the stack.
 *
 * @author xDark
 */
public final class ShortPushProcessor implements InstructionProcessor<IntInsnNode> {

	@Override
	public Result execute(IntInsnNode insn, ExecutionContext<?> ctx) {
		ctx.getStack().pushInt(insn.operand);
		return Result.CONTINUE;
	}
}
