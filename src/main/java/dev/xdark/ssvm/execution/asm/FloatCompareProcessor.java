package dev.xdark.ssvm.execution.asm;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.value.IntValue;
import dev.xdark.ssvm.value.Value;
import lombok.val;
import org.objectweb.asm.tree.AbstractInsnNode;

/**
 * Compares two floats.
 * Pushes provided value if one of the values is NaN.
 *
 * @author xDark
 */
public final class FloatCompareProcessor implements InstructionProcessor<AbstractInsnNode> {

	private final Value nan;

	/**
	 * @param nan
	 * 		Value to be pushed to the stack
	 * 		if one of the floats is {@code NaN}
	 */
	public FloatCompareProcessor(int nan) {
		this.nan = new IntValue(nan);
	}

	@Override
	public Result execute(AbstractInsnNode insn, ExecutionContext ctx) {
		val stack = ctx.getStack();
		val v2 = stack.pop().asFloat();
		val v1 = stack.pop().asFloat();
		if (Float.isNaN(v1) || Float.isNaN(v2)) {
			stack.push(nan);
		} else {
			stack.push(new IntValue(Float.compare(v1, v2)));
		}
		return Result.CONTINUE;
	}
}
