package dev.xdark.ssvm.execution.asm;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;
import org.objectweb.asm.tree.AbstractInsnNode;

/**
 * Duplicate the top one or two operand stack values
 * and insert two, three, or four values down.
 *
 * @author xDark
 */
public final class Dup2X2Processor implements InstructionProcessor<AbstractInsnNode> {

	@Override
	public Result execute(AbstractInsnNode insn, ExecutionContext ctx) {
		ctx.getStack().dup2x2();
		return Result.CONTINUE;
	}
}
