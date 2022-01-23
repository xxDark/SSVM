package dev.xdark.ssvm.execution.asm;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.value.*;
import lombok.val;
import org.objectweb.asm.tree.AbstractInsnNode;

/**
 * Loads value from an array.
 *
 * @author xDark
 */
public final class LoadArrayValueProcessor implements InstructionProcessor<AbstractInsnNode> {

	@Override
	public Result execute(AbstractInsnNode insn, ExecutionContext ctx) {
		val stack = ctx.getStack();
		int index = stack.pop().asInt();
		val helper = ctx.getHelper();
		val array = helper.checkNotNullArray(stack.pop());
		ctx.getHelper().rangeCheck(array, index);
		stack.push(array.getValue(index));
		return Result.CONTINUE;
	}
}
