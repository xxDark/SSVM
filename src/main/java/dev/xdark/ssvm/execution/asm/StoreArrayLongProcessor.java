package dev.xdark.ssvm.execution.asm;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.value.ArrayValue;
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
		val value = stack.popWide().asLong();
		int index = stack.pop().asInt();
		val array = stack.<ArrayValue>pop();
		ctx.getHelper().rangeCheck(array, index);
		array.setLong(index, value);
		return Result.CONTINUE;
	}
}
