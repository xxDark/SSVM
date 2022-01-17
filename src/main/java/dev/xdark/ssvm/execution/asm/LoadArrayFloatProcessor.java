package dev.xdark.ssvm.execution.asm;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.value.ArrayValue;
import dev.xdark.ssvm.value.FloatValue;
import dev.xdark.ssvm.value.IntValue;
import lombok.val;
import org.objectweb.asm.tree.AbstractInsnNode;

/**
 * Loads float from an array.
 *
 * @author xDark
 */
public final class LoadArrayFloatProcessor implements InstructionProcessor<AbstractInsnNode> {

	@Override
	public Result execute(AbstractInsnNode insn, ExecutionContext ctx) {
		val stack = ctx.getStack();
		int index = stack.pop().asInt();
		val array = stack.<ArrayValue>pop();
		ctx.getHelper().rangeCheck(array, index);
		stack.push(new FloatValue(array.getFloat(index)));
		return Result.CONTINUE;
	}
}
