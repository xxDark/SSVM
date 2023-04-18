package dev.xdark.ssvm.execution.asm;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.execution.Stack;
import dev.xdark.ssvm.util.MathUtil;
import org.objectweb.asm.tree.AbstractInsnNode;

/**
 * Compares two doubles.
 * Pushes provided value if one of the values is NaN.
 *
 * @author xDark
 */
public final class DoubleCompareProcessor implements InstructionProcessor<AbstractInsnNode> {

	private final int nan;

	/**
	 * @param nan Value to be pushed to the stack
	 *            if one of the doubles is {@code NaN}.
	 */
	public DoubleCompareProcessor(int nan) {
		this.nan = nan;
	}

	@Override
	public Result execute(AbstractInsnNode insn, ExecutionContext<?> ctx) {
		Stack stack = ctx.getStack();
		double v2 = stack.popDouble();
		double v1 = stack.popDouble();
		stack.pushInt(MathUtil.compareDouble(v1, v2, nan));
		return Result.CONTINUE;
	}
}
