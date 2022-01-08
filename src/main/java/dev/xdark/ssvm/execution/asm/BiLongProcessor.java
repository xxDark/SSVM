package dev.xdark.ssvm.execution.asm;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.util.BiLongToLongFunction;
import dev.xdark.ssvm.value.LongValue;
import org.objectweb.asm.tree.AbstractInsnNode;

/**
 * Performs operation on two longs.
 *
 * @author xDark
 */
public final class BiLongProcessor implements InstructionProcessor<AbstractInsnNode> {

	private final BiLongToLongFunction op;

	/**
	 * @param op
	 * 		Longs processor.
	 */
	public BiLongProcessor(BiLongToLongFunction op) {
		this.op = op;
	}

	@Override
	public Result execute(AbstractInsnNode insn, ExecutionContext ctx) {
		var stack = ctx.getStack();
		var v2 = stack.popWide().asLong();
		var v1 = stack.popWide().asLong();
		stack.pushWide(new LongValue(op.apply(v1, v2)));
		return Result.CONTINUE;
	}
}
