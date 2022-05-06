package dev.xdark.ssvm.execution.asm;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.execution.Stack;
import dev.xdark.ssvm.value.DoubleValue;
import org.objectweb.asm.tree.AbstractInsnNode;

/**
 * Negates double on the stack.
 *
 * @author xDark
 */
public final class NegativeDoubleProcessor implements InstructionProcessor<AbstractInsnNode> {

	@Override
	public Result execute(AbstractInsnNode insn, ExecutionContext ctx) {
		Stack stack = ctx.getStack();
		stack.pushWide(new DoubleValue(-stack.popWide().asDouble()));
		return Result.CONTINUE;
	}
}
