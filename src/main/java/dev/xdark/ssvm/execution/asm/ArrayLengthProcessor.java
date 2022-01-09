package dev.xdark.ssvm.execution.asm;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.value.ArrayValue;
import dev.xdark.ssvm.value.IntValue;
import org.objectweb.asm.tree.AbstractInsnNode;

/**
 * Pushes array length onto the stack.
 *
 * @author xDark
 */
public final class ArrayLengthProcessor implements InstructionProcessor<AbstractInsnNode> {

	@Override
	public Result execute(AbstractInsnNode insn, ExecutionContext ctx) {
		var stack = ctx.getStack();
		var array = stack.<ArrayValue>pop();
		stack.push(new IntValue(ctx.getVM().getMemoryManager().readArrayLength(array)));
		return Result.CONTINUE;
	}
}
