package dev.xdark.ssvm.execution.asm;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.value.ArrayValue;
import dev.xdark.ssvm.value.DoubleValue;
import dev.xdark.ssvm.value.FloatValue;
import dev.xdark.ssvm.value.LongValue;
import org.objectweb.asm.tree.AbstractInsnNode;

/**
 * Loads long from an array.
 *
 * @author xDark
 */
public final class LoadArrayLongProcessor implements InstructionProcessor<AbstractInsnNode> {

	@Override
	public Result execute(AbstractInsnNode insn, ExecutionContext ctx) {
		var stack = ctx.getStack();
		var index = stack.pop().asInt();
		var array = stack.<ArrayValue>pop();
		stack.pushWide(new LongValue(array.getLong(index)));
		return Result.CONTINUE;
	}
}
