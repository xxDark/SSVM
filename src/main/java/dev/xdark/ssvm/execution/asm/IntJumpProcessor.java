package dev.xdark.ssvm.execution.asm;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.util.AsmUtil;
import org.objectweb.asm.tree.JumpInsnNode;

import java.util.function.IntPredicate;

/**
 * Jumps if predicate on the int succeeds.
 *
 * @author xDark
 */
public final class IntJumpProcessor implements InstructionProcessor<JumpInsnNode> {

	private final IntPredicate condition;

	/**
	 * @param condition
	 * 		Predicate to check.
	 */
	public IntJumpProcessor(IntPredicate condition) {
		this.condition = condition;
	}

	@Override
	public Result execute(JumpInsnNode insn, ExecutionContext ctx) {
		if (condition.test(ctx.getStack().pop().asInt())) {
			ctx.setInsnPosition(AsmUtil.getIndex(insn.label));
		}
		return Result.CONTINUE;
	}
}
