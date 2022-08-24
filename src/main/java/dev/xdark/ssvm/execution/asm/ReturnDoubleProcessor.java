package dev.xdark.ssvm.execution.asm;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;
import org.objectweb.asm.tree.AbstractInsnNode;

/**
 * Stops execution and sets double as the result.
 *
 * @author xDark
 */
public final class ReturnDoubleProcessor implements InstructionProcessor<AbstractInsnNode> {

	@Override
	public Result execute(AbstractInsnNode insn, ExecutionContext<?> ctx) {
		ctx.setResult(ctx.getStack().popDouble());
		return Result.ABORT;
	}
}
