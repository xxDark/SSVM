package dev.xdark.ssvm.execution.rewrite.array;

import dev.xdark.ssvm.asm.DelegatingInsnNode;
import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.execution.Stack;
import org.objectweb.asm.tree.IntInsnNode;

/**
 * Fast-path for NEWARRAY instruction.
 *
 * @author xDark
 */
public final class ShortArrayProcessor implements InstructionProcessor<DelegatingInsnNode<IntInsnNode>> {

	@Override
	public Result execute(DelegatingInsnNode<IntInsnNode> insn, ExecutionContext<?> ctx) {
		Stack stack = ctx.getStack();
		int length = stack.popInt();
		stack.pushReference(ctx.getOperations().allocateShortArray(length));
		return Result.CONTINUE;
	}
}
