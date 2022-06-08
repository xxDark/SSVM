package dev.xdark.ssvm.execution.asm;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.execution.Stack;
import dev.xdark.ssvm.value.ObjectValue;
import org.objectweb.asm.tree.AbstractInsnNode;

/**
 * Loads char from an array.
 *
 * @author xDark
 */
public final class LoadArrayCharProcessor implements InstructionProcessor<AbstractInsnNode> {

	@Override
	public Result execute(AbstractInsnNode insn, ExecutionContext ctx) {
		Stack stack = ctx.getStack();
		int index = stack.popInt();
		ObjectValue array = stack.pop();
		stack.pushInt(ctx.getOperations().arrayLoadChar(array, index));
		return Result.CONTINUE;
	}
}
