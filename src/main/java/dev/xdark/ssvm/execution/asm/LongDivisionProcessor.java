package dev.xdark.ssvm.execution.asm;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.execution.Stack;
import org.objectweb.asm.tree.AbstractInsnNode;

/**
 * Special processor for {@code LDIV}.
 *
 * @author xDark
 */
public class LongDivisionProcessor implements InstructionProcessor<AbstractInsnNode> {
	@Override
	public Result execute(AbstractInsnNode insn, ExecutionContext<?> ctx) {
		Stack stack = ctx.getStack();
		long v2 = stack.popLong();
		if (v2 == 0L) {
			ctx.getHelper().throwException(ctx.getSymbols().java_lang_ArithmeticException(), "/ by zero");
		}
		long v1 = stack.popLong();
		stack.pushLong(v1 / v2);
		return Result.CONTINUE;
	}
}
