package dev.xdark.ssvm.execution.asm;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.execution.Stack;
import dev.xdark.ssvm.value.IntValue;
import dev.xdark.ssvm.value.Value;
import org.objectweb.asm.tree.AbstractInsnNode;

/**
 * Converts int to char.
 *
 * @author xDark
 */
public final class IntToCharProcessor implements InstructionProcessor<AbstractInsnNode> {

	@Override
	public Result execute(AbstractInsnNode insn, ExecutionContext ctx) {
		Stack stack = ctx.getStack();
		Value v = stack.peek();
		int i = v.asInt();
		char c = (char) i;
		if (i != c) {
			stack.pop();
			stack.pushInt(c);
		}
		return Result.CONTINUE;
	}
}
