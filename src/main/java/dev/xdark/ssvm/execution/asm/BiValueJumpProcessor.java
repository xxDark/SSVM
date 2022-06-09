package dev.xdark.ssvm.execution.asm;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.execution.Stack;
import dev.xdark.ssvm.util.AsmUtil;
import dev.xdark.ssvm.value.Value;
import org.objectweb.asm.tree.JumpInsnNode;

import java.util.function.BiPredicate;

/**
 * Jumps if predicate on two objects succeeds.
 *
 * @author xDark
 */
public final class BiValueJumpProcessor implements InstructionProcessor<JumpInsnNode> {

	private final BiPredicate<Value, Value> condition;

	/**
	 * @param condition Predicate to check.
	 */
	public BiValueJumpProcessor(BiPredicate<Value, Value> condition) {
		this.condition = condition;
	}

	@Override
	public Result execute(JumpInsnNode insn, ExecutionContext ctx) {
		Stack stack = ctx.getStack();
		Value v2 = stack.pop();
		Value v1 = stack.pop();
		if (condition.test(v1, v2)) {
			ctx.setInsnPosition(AsmUtil.getIndex(insn.label));
		}
		return Result.CONTINUE;
	}
}
