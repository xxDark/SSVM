package dev.xdark.ssvm.execution.asm;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.util.BiIntToIntFunction;
import dev.xdark.ssvm.util.LongIntToLongFunction;
import dev.xdark.ssvm.value.IntValue;
import dev.xdark.ssvm.value.LongValue;
import org.objectweb.asm.tree.AbstractInsnNode;

/**
 * Performs operation on long and int.
 *
 * @author xDark
 */
public final class LongIntProcessor implements InstructionProcessor<AbstractInsnNode> {

	private final LongIntToLongFunction op;

	/**
	 * @param op
	 * 		Ints processor.
	 */
	public LongIntProcessor(LongIntToLongFunction op) {
		this.op = op;
	}

	@Override
	public Result execute(AbstractInsnNode insn, ExecutionContext ctx) {
		var stack = ctx.getStack();
		var v2 = stack.pop().asInt();
		var v1 = stack.popWide().asLong();
		stack.push(new LongValue(op.apply(v1, v2)));
		return Result.CONTINUE;
	}
}
