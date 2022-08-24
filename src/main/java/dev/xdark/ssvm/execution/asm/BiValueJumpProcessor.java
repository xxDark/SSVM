package dev.xdark.ssvm.execution.asm;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.execution.Stack;
import dev.xdark.ssvm.util.AsmUtil;
import dev.xdark.ssvm.value.ObjectValue;
import org.objectweb.asm.tree.JumpInsnNode;

import java.util.function.BiPredicate;

/**
 * Jumps if predicate on two objects succeeds.
 *
 * @author xDark
 */
public final class BiValueJumpProcessor implements InstructionProcessor<JumpInsnNode> {

	private final BiPredicate<ObjectValue, ObjectValue> condition;

	/**
	 * @param condition Predicate to check.
	 */
	public BiValueJumpProcessor(BiPredicate<ObjectValue, ObjectValue> condition) {
		this.condition = condition;
	}

	@Override
	public Result execute(JumpInsnNode insn, ExecutionContext<?> ctx) {
		Stack stack = ctx.getStack();
		ObjectValue v2 = stack.popReference();
		ObjectValue v1 = stack.popReference();
		if (condition.test(v1, v2)) {
			ctx.setInsnPosition(AsmUtil.getIndex(insn.label));
		}
		return Result.CONTINUE;
	}
}
