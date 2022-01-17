package dev.xdark.ssvm.execution.asm;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.value.ArrayValue;
import dev.xdark.ssvm.value.ObjectValue;
import lombok.val;
import org.objectweb.asm.tree.AbstractInsnNode;

/**
 * Stores value into an array.
 *
 * @author xDark
 */
public final class StoreArrayValueProcessor implements InstructionProcessor<AbstractInsnNode> {

	@Override
	public Result execute(AbstractInsnNode insn, ExecutionContext ctx) {
		val stack = ctx.getStack();
		val value = stack.pop();
		int index = stack.pop().asInt();
		val array = stack.<ArrayValue>pop();
		ctx.getHelper().rangeCheck(array, index);
		array.setValue(index, (ObjectValue) value);
		return Result.CONTINUE;
	}
}
