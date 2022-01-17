package dev.xdark.ssvm.execution.asm;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.value.FloatValue;
import dev.xdark.ssvm.value.IntValue;
import lombok.val;
import org.objectweb.asm.tree.AbstractInsnNode;

/**
 * Converts long to float.
 *
 * @author xDark
 */
public final class LongToFloatProcessor implements InstructionProcessor<AbstractInsnNode> {

	@Override
	public Result execute(AbstractInsnNode insn, ExecutionContext ctx) {
		val stack = ctx.getStack();
		stack.push(new FloatValue(stack.popWide().asFloat()));
		return Result.CONTINUE;
	}
}
