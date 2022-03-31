package dev.xdark.ssvm.execution.asm;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.jit.JitHelper;
import lombok.val;
import org.objectweb.asm.tree.AbstractInsnNode;

/**
 * Stores long into an array.
 *
 * @author xDark
 */
public final class StoreArrayLongProcessor implements InstructionProcessor<AbstractInsnNode> {

	@Override
	public Result execute(AbstractInsnNode insn, ExecutionContext ctx) {
		val stack = ctx.getStack();
		val value = stack.popWide();
		val index = stack.pop();
		val array = stack.pop();
		JitHelper.arrayStoreLong(array, index, value, ctx);
		return Result.CONTINUE;
	}
}
