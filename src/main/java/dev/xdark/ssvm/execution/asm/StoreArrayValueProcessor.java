package dev.xdark.ssvm.execution.asm;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.value.ArrayValue;
import dev.xdark.ssvm.value.ObjectValue;
import org.objectweb.asm.tree.AbstractInsnNode;

/**
 * Stores value into an array.
 *
 * @author xDark
 */
public final class StoreArrayValueProcessor implements InstructionProcessor<AbstractInsnNode> {

	@Override
	public Result execute(AbstractInsnNode insn, ExecutionContext ctx) {
		var stack = ctx.getStack();
		var value = stack.pop();
		var index = stack.pop().asInt();
		var array = stack.<ArrayValue>pop();
		ctx.getHelper().rangeCheck(array, index);
		array.setValue(index, (ObjectValue) value);
		return Result.CONTINUE;
	}
}
