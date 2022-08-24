package dev.xdark.ssvm.execution.asm;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.execution.Stack;
import dev.xdark.ssvm.util.BiLongToLongFunction;
import org.objectweb.asm.tree.AbstractInsnNode;

/**
 * Performs operation on two longs.
 *
 * @author xDark
 */
public final class BiLongProcessor implements InstructionProcessor<AbstractInsnNode> {

	private final BiLongToLongFunction op;

	/**
	 * @param op Longs processor.
	 */
	public BiLongProcessor(BiLongToLongFunction op) {
		this.op = op;
	}

	@Override
	public Result execute(AbstractInsnNode insn, ExecutionContext<?> ctx) {
		Stack stack = ctx.getStack();
		long v2 = stack.popLong();
		long v1 = stack.popLong();
		stack.pushLong(op.apply(v1, v2));
		return Result.CONTINUE;
	}
}
