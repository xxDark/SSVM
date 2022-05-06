package dev.xdark.ssvm.execution.asm;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.execution.Stack;
import dev.xdark.ssvm.util.BiDoubleToDoubleFunction;
import dev.xdark.ssvm.value.DoubleValue;
import org.objectweb.asm.tree.AbstractInsnNode;

/**
 * Performs operation on two doubles.
 *
 * @author xDark
 */
public final class BiDoubleProcessor implements InstructionProcessor<AbstractInsnNode> {

	private final BiDoubleToDoubleFunction op;

	/**
	 * @param op
	 * 		Double processor.
	 */
	public BiDoubleProcessor(BiDoubleToDoubleFunction op) {
		this.op = op;
	}

	@Override
	public Result execute(AbstractInsnNode insn, ExecutionContext ctx) {
		Stack stack = ctx.getStack();
		double v2 = stack.popWide().asDouble();
		double v1 = stack.popWide().asDouble();
		stack.pushWide(new DoubleValue(op.apply(v1, v2)));
		return Result.CONTINUE;
	}
}
