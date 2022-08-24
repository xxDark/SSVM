package dev.xdark.ssvm.execution.asm;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;
import org.objectweb.asm.tree.AbstractInsnNode;

/**
 * Duplicate the top operand stack value and insert two or three values down.
 *
 * @author xDark
 */
public final class DupX2Processor implements InstructionProcessor<AbstractInsnNode> {

	@Override
	public Result execute(AbstractInsnNode insn, ExecutionContext<?> ctx) {
		ctx.getStack().dupx2();
		return Result.CONTINUE;
	}
}
