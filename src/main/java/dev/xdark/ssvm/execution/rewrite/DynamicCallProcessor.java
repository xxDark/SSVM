package dev.xdark.ssvm.execution.rewrite;

import dev.xdark.ssvm.asm.LinkedDynamicCallNode;
import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.execution.Stack;
import dev.xdark.ssvm.value.Value;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;

/**
 * Processor for linked invokedynamic instructions.
 *
 * @author xDark
 */
public final class DynamicCallProcessor implements InstructionProcessor<LinkedDynamicCallNode> {

	@Override
	public Result execute(LinkedDynamicCallNode insn, ExecutionContext ctx) {
		InvokeDynamicInsnNode delegate = insn.getDelegate();
		int descriptorSize = insn.getDescriptorArgsSize();
		int x = descriptorSize + 1;
		Value[] locals = new Value[x];
		Stack stack = ctx.getStack();
		while (descriptorSize-- != 0) {
			locals[--x] = stack.pop();
		}
		Value invoked = ctx.getInvokeDynamicLinker().dynamicCall(locals, delegate.desc, insn.getMethodHandle());
		if (!invoked.isVoid()) {
			stack.pushGeneric(invoked);
		}
		return Result.CONTINUE;
	}
}
