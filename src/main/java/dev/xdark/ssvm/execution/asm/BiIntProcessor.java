package dev.xdark.ssvm.execution.asm;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.util.BiIntToIntFunction;
import dev.xdark.ssvm.value.IntValue;
import org.objectweb.asm.tree.AbstractInsnNode;

/**
 * Performs operation on two ints.
 *
 * @author xDark
 */
public final class BiIntProcessor implements InstructionProcessor<AbstractInsnNode> {

	private final BiIntToIntFunction op;

	/**
	 * @param op
	 * 		Ints processor.
	 */
	public BiIntProcessor(BiIntToIntFunction op) {
		this.op = op;
	}

	@Override
	public Result execute(AbstractInsnNode insn, ExecutionContext ctx) {
		var stack = ctx.getStack();
		var v2 = stack.pop().asInt();
		var v1 = stack.pop().asInt();
		stack.push(new IntValue(op.apply(v1, v2)));
		return Result.CONTINUE;
	}
}
