package dev.xdark.ssvm.execution.asm;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.execution.Stack;
import dev.xdark.ssvm.value.ObjectValue;
import org.objectweb.asm.tree.AbstractInsnNode;

/**
 * Stores byte into an array.
 *
 * @author xDark
 */
public final class StoreArrayByteProcessor implements InstructionProcessor<AbstractInsnNode> {

	@Override
	public Result execute(AbstractInsnNode insn, ExecutionContext<?> ctx) {
		Stack stack = ctx.getStack();
		byte value = stack.popByte();
		int index = stack.popInt();
		ObjectValue array = stack.popReference();
		ctx.getOperations().arrayStoreByte(array, index, value);
		return Result.CONTINUE;
	}
}
