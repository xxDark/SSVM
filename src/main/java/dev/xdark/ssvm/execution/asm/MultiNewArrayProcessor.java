package dev.xdark.ssvm.execution.asm;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.execution.Stack;
import dev.xdark.ssvm.mirror.type.SimpleArrayClass;
import dev.xdark.ssvm.mirror.type.JavaClass;
import dev.xdark.ssvm.util.Helper;
import org.objectweb.asm.tree.MultiANewArrayInsnNode;

/**
 * Pushes multidimensional array.
 *
 * @author xDark
 */
public final class MultiNewArrayProcessor implements InstructionProcessor<MultiANewArrayInsnNode> {

	@Override
	public Result execute(MultiANewArrayInsnNode insn, ExecutionContext<?> ctx) {
		int dimensions = insn.dims;
		Helper helper = ctx.getHelper();
		JavaClass type = helper.tryFindClass(ctx.getClassLoader(), insn.desc, false);
		Stack stack = ctx.getStack();
		int[] lengths = new int[dimensions];
		while (dimensions-- != 0) {
			lengths[dimensions] = stack.popInt();
		}
		stack.pushReference(helper.newMultiArray((SimpleArrayClass) type, lengths));
		return Result.CONTINUE;
	}
}
