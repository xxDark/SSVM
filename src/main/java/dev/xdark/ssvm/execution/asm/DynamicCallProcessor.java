package dev.xdark.ssvm.execution.asm;

import dev.xdark.ssvm.asm.LinkedDynamicCallNode;
import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.util.InvokeDynamicLinker;
import dev.xdark.ssvm.value.Value;
import lombok.val;

/**
 * Processor for linked invokedynamic instructions.
 *
 * @author xDark
 */
public final class DynamicCallProcessor implements InstructionProcessor<LinkedDynamicCallNode> {

	@Override
	public Result execute(LinkedDynamicCallNode insn, ExecutionContext ctx) {
		val delegate = insn.getDelegate();
		val args = insn.getDescriptorArgs();
		int localsLength = args.length;
		int x = localsLength + 1;
		val locals = new Value[x];
		val stack = ctx.getStack();
		while (localsLength-- != 0) {
			locals[--x] = stack.popGeneric();
		}
		val invoked = InvokeDynamicLinker.dynamicCall(locals, delegate.desc, insn.getMethodHandle(), ctx);
		if (!invoked.isVoid()) {
			stack.pushGeneric(invoked);
		}
		return Result.CONTINUE;
	}
}
