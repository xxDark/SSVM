package dev.xdark.ssvm.execution.asm;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.value.IntValue;
import dev.xdark.ssvm.value.Value;
import lombok.val;
import org.objectweb.asm.tree.AbstractInsnNode;

/**
 * Compares two doubles.
 * Pushes provided value if one of the values is NaN.
 *
 * @author xDark
 */
public final class DoubleCompareProcessor implements InstructionProcessor<AbstractInsnNode> {

	private final Value nan;

	/**
	 * @param nan
	 * 		Value to be pushed to the stack
	 * 		if one of the doubles is {@code NaN}
	 */
	public DoubleCompareProcessor(int nan) {
		this.nan = IntValue.of(nan);
	}

	@Override
	public Result execute(AbstractInsnNode insn, ExecutionContext ctx) {
		val stack = ctx.getStack();
		val v2 = stack.popWide().asDouble();
		val v1 = stack.popWide().asDouble();
		if (Double.isNaN(v1) || Double.isNaN(v2)) {
			stack.push(nan);
		} else {
			stack.push(IntValue.of(Double.compare(v1, v2)));
		}
		return Result.CONTINUE;
	}
}
