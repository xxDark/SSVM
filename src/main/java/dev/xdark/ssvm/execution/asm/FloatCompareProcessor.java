package dev.xdark.ssvm.execution.asm;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.execution.Stack;
import dev.xdark.ssvm.jit.JitHelper;
import dev.xdark.ssvm.value.Value;
import org.objectweb.asm.tree.AbstractInsnNode;

/**
 * Compares two floats.
 * Pushes provided value if one of the values is NaN.
 *
 * @author xDark
 */
public final class FloatCompareProcessor implements InstructionProcessor<AbstractInsnNode> {

	private final int nan;

	/**
	 * @param nan Value to be pushed to the stack
	 *            if one of the floats is {@code NaN}.
	 */
	public FloatCompareProcessor(int nan) {
		this.nan = nan;
	}

	@Override
	public Result execute(AbstractInsnNode insn, ExecutionContext ctx) {
		Stack stack = ctx.getStack();
		Value v2 = stack.pop();
		Value v1 = stack.pop();
		stack.push(JitHelper.compareFloat(v1, v2, nan));
		return Result.CONTINUE;
	}
}
