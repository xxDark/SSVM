package dev.xdark.ssvm.execution.asm;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.value.ArrayValue;
import dev.xdark.ssvm.value.IntValue;
import lombok.val;
import org.objectweb.asm.tree.AbstractInsnNode;

/**
 * Loads int from an array.
 *
 * @author xDark
 */
public final class LoadArrayIntProcessor implements InstructionProcessor<AbstractInsnNode> {

	@Override
	public Result execute(AbstractInsnNode insn, ExecutionContext ctx) {
		val stack = ctx.getStack();
		int index = stack.pop().asInt();
		val helper = ctx.getHelper();
		val array = helper.checkNotNullArray(stack.pop());
		helper.rangeCheck(array, index);
		stack.push(new IntValue(array.getInt(index)));
		return Result.CONTINUE;
	}
}
