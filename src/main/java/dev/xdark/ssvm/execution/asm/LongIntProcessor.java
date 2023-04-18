package dev.xdark.ssvm.execution.asm;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.execution.Stack;
import dev.xdark.ssvm.util.LongIntToLongFunction;
import org.objectweb.asm.tree.AbstractInsnNode;

/**
 * Performs operation on long and int.
 *
 * @author xDark
 */
public final class LongIntProcessor implements InstructionProcessor<AbstractInsnNode> {

	private final LongIntToLongFunction op;

	/**
	 * @param op Ints processor.
	 */
	public LongIntProcessor(LongIntToLongFunction op) {
		this.op = op;
	}

	@Override
	public Result execute(AbstractInsnNode insn, ExecutionContext<?> ctx) {
		Stack stack = ctx.getStack();
		int v2 = stack.popInt();
		long v1 = stack.popLong();
		stack.pushLong(op.apply(v1, v2));
		return Result.CONTINUE;
	}
}
