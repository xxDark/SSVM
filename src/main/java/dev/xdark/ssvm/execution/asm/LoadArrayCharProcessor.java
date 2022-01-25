package dev.xdark.ssvm.execution.asm;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.jit.JitHelper;
import lombok.val;
import org.objectweb.asm.tree.AbstractInsnNode;

/**
 * Loads char from an array.
 *
 * @author xDark
 */
public final class LoadArrayCharProcessor implements InstructionProcessor<AbstractInsnNode> {

	@Override
	public Result execute(AbstractInsnNode insn, ExecutionContext ctx) {
		val stack = ctx.getStack();
		val index = stack.pop();
		val array = stack.pop();
		stack.push(JitHelper.arrayLoadChar(array, index, ctx));
		return Result.CONTINUE;
	}
}
