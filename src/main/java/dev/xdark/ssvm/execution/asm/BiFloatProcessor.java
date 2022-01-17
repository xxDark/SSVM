package dev.xdark.ssvm.execution.asm;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.util.BiFloatToFloatFunction;
import dev.xdark.ssvm.value.FloatValue;
import lombok.val;
import org.objectweb.asm.tree.AbstractInsnNode;

/**
 * Performs operation on two floats.
 *
 * @author xDark
 */
public final class BiFloatProcessor implements InstructionProcessor<AbstractInsnNode> {

	private final BiFloatToFloatFunction op;

	/**
	 * @param op
	 * 		Double processor.
	 */
	public BiFloatProcessor(BiFloatToFloatFunction op) {
		this.op = op;
	}

	@Override
	public Result execute(AbstractInsnNode insn, ExecutionContext ctx) {
		val stack = ctx.getStack();
		val v2 = stack.pop().asFloat();
		val v1 = stack.pop().asFloat();
		stack.push(new FloatValue(op.apply(v1, v2)));
		return Result.CONTINUE;
	}
}
