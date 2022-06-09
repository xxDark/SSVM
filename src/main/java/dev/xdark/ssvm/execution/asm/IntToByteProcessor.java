package dev.xdark.ssvm.execution.asm;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.execution.Stack;
import dev.xdark.ssvm.value.IntValue;
import dev.xdark.ssvm.value.Value;
import org.objectweb.asm.tree.AbstractInsnNode;

/**
 * Converts int to byte.
 *
 * @author xDark
 */
public final class IntToByteProcessor implements InstructionProcessor<AbstractInsnNode> {

	@Override
	public Result execute(AbstractInsnNode insn, ExecutionContext ctx) {
		Stack stack = ctx.getStack();
		Value v = stack.peek();
		byte b = v.asByte();
		if (v.asInt() != b) {
			stack.pop();
			stack.pushInt(b);
		}
		return Result.CONTINUE;
	}
}
