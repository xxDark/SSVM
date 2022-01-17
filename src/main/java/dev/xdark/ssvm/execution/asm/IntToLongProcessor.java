package dev.xdark.ssvm.execution.asm;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.value.LongValue;
import lombok.val;
import org.objectweb.asm.tree.AbstractInsnNode;

/**
 * Converts int to long.
 *
 * @author xDark
 */
public final class IntToLongProcessor implements InstructionProcessor<AbstractInsnNode> {

	@Override
	public Result execute(AbstractInsnNode insn, ExecutionContext ctx) {
		val stack = ctx.getStack();
		stack.pushWide(new LongValue(stack.pop().asLong()));
		return Result.CONTINUE;
	}
}
