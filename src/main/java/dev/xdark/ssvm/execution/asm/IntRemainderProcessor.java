package dev.xdark.ssvm.execution.asm;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.execution.Stack;
import org.objectweb.asm.tree.AbstractInsnNode;

/**
 * Special processor for {@code IREM}.
 *
 * @author xDark
 */
public class IntRemainderProcessor implements InstructionProcessor<AbstractInsnNode> {
	@Override
	public Result execute(AbstractInsnNode insn, ExecutionContext<?> ctx) {
		Stack stack = ctx.getStack();
		int v2 = stack.popInt();
		if (v2 == 0) {
			ctx.getOperations().throwException(ctx.getSymbols().java_lang_ArithmeticException(), "/ by zero");
			return Result.ABORT;
		}
		int v1 = stack.popInt();
		stack.pushInt(v1 % v2);
		return Result.CONTINUE;
	}
}
