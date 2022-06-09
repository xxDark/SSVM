package dev.xdark.ssvm.execution.asm;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.execution.Stack;
import org.objectweb.asm.tree.AbstractInsnNode;

/**
 * Compares two long values.
 *
 * @author xDark
 * @see Long#compare(long, long)
 */
public final class LongCompareProcessor implements InstructionProcessor<AbstractInsnNode> {

	@Override
	public Result execute(AbstractInsnNode insn, ExecutionContext ctx) {
		Stack stack = ctx.getStack();
		long v2 = stack.popLong();
		long v1 = stack.popLong();
		stack.pushInt(Long.compare(v1, v2));
		return Result.CONTINUE;
	}
}
