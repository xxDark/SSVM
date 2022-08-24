package dev.xdark.ssvm.execution.asm;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.execution.Stack;
import dev.xdark.ssvm.value.ObjectValue;
import org.objectweb.asm.tree.AbstractInsnNode;

/**
 * Loads short from an array.
 *
 * @author xDark
 */
public final class LoadArrayShortProcessor implements InstructionProcessor<AbstractInsnNode> {

	@Override
	public Result execute(AbstractInsnNode insn, ExecutionContext ctx) {
		Stack stack = ctx.getStack();
		int index = stack.popInt();
		ObjectValue array = stack.popReference();
		stack.pushInt(ctx.getOperations().arrayLoadShort(array, index));
		return Result.CONTINUE;
	}
}
