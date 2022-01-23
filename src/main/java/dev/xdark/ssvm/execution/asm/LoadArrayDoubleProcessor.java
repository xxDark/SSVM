package dev.xdark.ssvm.execution.asm;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.value.ArrayValue;
import dev.xdark.ssvm.value.DoubleValue;
import lombok.val;
import org.objectweb.asm.tree.AbstractInsnNode;

/**
 * Loads double from an array.
 *
 * @author xDark
 */
public final class LoadArrayDoubleProcessor implements InstructionProcessor<AbstractInsnNode> {

	@Override
	public Result execute(AbstractInsnNode insn, ExecutionContext ctx) {
		val stack = ctx.getStack();
		int index = stack.pop().asInt();
		val helper = ctx.getHelper();
		val array = helper.checkNotNullArray(stack.pop());
		helper.rangeCheck(array, index);
		stack.pushWide(new DoubleValue(array.getDouble(index)));
		return Result.CONTINUE;
	}
}
