package dev.xdark.ssvm.execution.asm;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.value.ArrayValue;
import dev.xdark.ssvm.value.DoubleValue;
import org.objectweb.asm.tree.AbstractInsnNode;

/**
 * Loads double from an array.
 *
 * @author xDark
 */
public final class LoadArrayDoubleProcessor implements InstructionProcessor<AbstractInsnNode> {

	@Override
	public Result execute(AbstractInsnNode insn, ExecutionContext ctx) {
		var stack = ctx.getStack();
		var index = stack.pop().asInt();
		var array = stack.<ArrayValue>pop();
		stack.pushWide(new DoubleValue(array.getDouble(index)));
		return Result.CONTINUE;
	}
}
