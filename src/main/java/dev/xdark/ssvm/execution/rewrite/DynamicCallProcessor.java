package dev.xdark.ssvm.execution.rewrite;

import dev.xdark.ssvm.asm.LinkedDynamicCallNode;
import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.execution.Stack;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;

/**
 * Processor for linked invokedynamic instructions.
 *
 * @author xDark
 */
public final class DynamicCallProcessor implements InstructionProcessor<LinkedDynamicCallNode> {

	@Override
	public Result execute(LinkedDynamicCallNode insn, ExecutionContext<?> ctx) {
		InvokeDynamicInsnNode delegate = insn.getDelegate();
		Stack stack = ctx.getStack();
		ctx.getInvokeDynamicLinker().dynamicCall(stack, delegate.desc, insn.getMethodHandle(), stack);
		return Result.CONTINUE;
	}
}
