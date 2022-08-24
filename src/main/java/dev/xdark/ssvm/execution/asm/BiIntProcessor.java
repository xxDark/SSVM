package dev.xdark.ssvm.execution.asm;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.execution.Stack;
import dev.xdark.ssvm.util.BiIntToIntFunction;
import org.objectweb.asm.tree.AbstractInsnNode;

/**
 * Performs operation on two ints.
 *
 * @author xDark
 */
public final class BiIntProcessor implements InstructionProcessor<AbstractInsnNode> {

	private final BiIntToIntFunction op;

	/**
	 * @param op Ints processor.
	 */
	public BiIntProcessor(BiIntToIntFunction op) {
		this.op = op;
	}

	@Override
	public Result execute(AbstractInsnNode insn, ExecutionContext<?> ctx) {
		Stack stack = ctx.getStack();
		int v2 = stack.popInt();
		int v1 = stack.popInt();
		stack.pushInt(op.apply(v1, v2));
		return Result.CONTINUE;
	}
}
