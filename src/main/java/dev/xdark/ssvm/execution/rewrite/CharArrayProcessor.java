package dev.xdark.ssvm.execution.rewrite;

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
public final class CharArrayProcessor implements InstructionProcessor<DelegatingInsnNode<IntInsnNode>> {

	@Override
	public Result execute(DelegatingInsnNode<IntInsnNode> insn, ExecutionContext<?> ctx) {
		Stack stack = ctx.getStack();
		int length = stack.popInt();
		stack.pushReference(ctx.getOperations().allocateCharArray(length));
		return Result.CONTINUE;
	}
}
