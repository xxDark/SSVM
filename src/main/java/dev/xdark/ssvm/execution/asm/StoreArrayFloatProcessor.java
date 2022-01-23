package dev.xdark.ssvm.execution.asm;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.value.ArrayValue;
import lombok.val;
import org.objectweb.asm.tree.AbstractInsnNode;

/**
 * Stores int into an array.
 *
 * @author xDark
 */
public final class StoreArrayFloatProcessor implements InstructionProcessor<AbstractInsnNode> {

	@Override
	public Result execute(AbstractInsnNode insn, ExecutionContext ctx) {
		val stack = ctx.getStack();
		val value = stack.pop().asFloat();
		int index = stack.pop().asInt();
		val helper = ctx.getHelper();
		val array = helper.checkNotNullArray(stack.pop());
		helper.rangeCheck(array, index);
		array.setFloat(index, value);
		return Result.CONTINUE;
	}
}
