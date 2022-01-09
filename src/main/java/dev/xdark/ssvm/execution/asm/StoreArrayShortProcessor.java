package dev.xdark.ssvm.execution.asm;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.value.ArrayValue;
import org.objectweb.asm.tree.AbstractInsnNode;

/**
 * Stores short into an array.
 *
 * @author xDark
 */
public final class StoreArrayShortProcessor implements InstructionProcessor<AbstractInsnNode> {

	@Override
	public Result execute(AbstractInsnNode insn, ExecutionContext ctx) {
		var stack = ctx.getStack();
		var value = stack.pop().asShort();
		var index = stack.pop().asInt();
		var array = stack.<ArrayValue>pop();
		array.setShort(index, value);
		return Result.CONTINUE;
	}
}
