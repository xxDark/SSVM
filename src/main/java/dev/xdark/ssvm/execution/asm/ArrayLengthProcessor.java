package dev.xdark.ssvm.execution.asm;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.execution.Stack;
import org.objectweb.asm.tree.AbstractInsnNode;

/**
 * Pushes array length onto the stack.
 *
 * @author xDark
 */
public final class ArrayLengthProcessor implements InstructionProcessor<AbstractInsnNode> {

	@Override
	public Result execute(AbstractInsnNode insn, ExecutionContext<?> ctx) {
		Stack stack = ctx.getStack();
		stack.pushInt(ctx.getOperations().getArrayLength(stack.popReference()));
		return Result.CONTINUE;
	}
}
