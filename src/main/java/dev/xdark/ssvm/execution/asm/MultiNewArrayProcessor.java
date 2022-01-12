package dev.xdark.ssvm.execution.asm;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.mirror.ArrayJavaClass;
import org.objectweb.asm.tree.MultiANewArrayInsnNode;

/**
 * Pushes multidimensional array.
 *
 * @author xDark
 */
public final class MultiNewArrayProcessor implements InstructionProcessor<MultiANewArrayInsnNode> {

	@Override
	public Result execute(MultiANewArrayInsnNode insn, ExecutionContext ctx) {
		var helper = ctx.getHelper();
		var type = helper.findClass(ctx.getOwner().getClassLoader(), insn.desc, true);
		var dimensions = insn.dims;
		var stack = ctx.getStack();
		var lengths = new int[dimensions];
		while (dimensions-- != 0) lengths[dimensions] = stack.pop().asInt();
		var array = helper.newMultiArray((ArrayJavaClass) type, lengths);
		stack.push(array);
		return Result.CONTINUE;
	}
}
