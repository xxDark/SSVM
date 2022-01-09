package dev.xdark.ssvm.execution.asm;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.value.ArrayValue;
import org.objectweb.asm.tree.AbstractInsnNode;

/**
 * Stores char into an array.
 *
 * @author xDark
 */
public final class StoreArrayCharProcessor implements InstructionProcessor<AbstractInsnNode> {

	@Override
	public Result execute(AbstractInsnNode insn, ExecutionContext ctx) {
		var stack = ctx.getStack();
		var value = stack.pop().asChar();
		var index = stack.pop().asInt();
		var array = stack.<ArrayValue>pop();
		ctx.getHelper().rangeCheck(array, index);
		array.setChar(index, value);
		return Result.CONTINUE;
	}
}
