package dev.xdark.ssvm.execution.asm;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.mirror.ArrayJavaClass;
import lombok.val;
import org.objectweb.asm.tree.MultiANewArrayInsnNode;

/**
 * Pushes multidimensional array.
 *
 * @author xDark
 */
public final class MultiNewArrayProcessor implements InstructionProcessor<MultiANewArrayInsnNode> {

	@Override
	public Result execute(MultiANewArrayInsnNode insn, ExecutionContext ctx) {
		val helper = ctx.getHelper();
		val type = helper.findClass(ctx.getOwner().getClassLoader(), insn.desc, true);
		int dimensions = insn.dims;
		val stack = ctx.getStack();
		val lengths = new int[dimensions];
		while (dimensions-- != 0) lengths[dimensions] = stack.pop().asInt();
		val array = helper.newMultiArray((ArrayJavaClass) type, lengths);
		stack.push(array);
		return Result.CONTINUE;
	}
}
