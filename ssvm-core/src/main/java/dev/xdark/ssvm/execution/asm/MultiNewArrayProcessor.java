package dev.xdark.ssvm.execution.asm;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.execution.Stack;
import dev.xdark.ssvm.mirror.type.SimpleArrayClass;
import dev.xdark.ssvm.mirror.type.JavaClass;
import dev.xdark.ssvm.operation.VMOperations;
import org.objectweb.asm.Type;
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
		VMOperations ops = ctx.getOperations();
		JavaClass type = ops.findClass(ctx.getOwner(), Type.getType(insn.desc), false);
		Stack stack = ctx.getStack();
		int[] lengths = new int[dimensions];
		while (dimensions-- != 0) {
			lengths[dimensions] = stack.popInt();
		}
		stack.pushReference(ops.allocateMultiArray((SimpleArrayClass) type, lengths));
		return Result.CONTINUE;
	}
}
