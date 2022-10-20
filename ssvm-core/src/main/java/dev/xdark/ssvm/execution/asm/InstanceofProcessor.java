package dev.xdark.ssvm.execution.asm;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.execution.Stack;
import dev.xdark.ssvm.mirror.type.JavaClass;
import dev.xdark.ssvm.operation.VMOperations;
import org.objectweb.asm.tree.TypeInsnNode;

/**
 * Performs instanceof check.
 */
public final class InstanceofProcessor implements InstructionProcessor<TypeInsnNode> {

	@Override
	public Result execute(TypeInsnNode insn, ExecutionContext<?> ctx) {
		VMOperations ops = ctx.getOperations();
		JavaClass klass = ops.findClass(ctx.getClassLoader(), insn.desc, false);
		Stack stack = ctx.getStack();
		boolean result = ops.isInstanceOf(stack.popReference(), klass);
		stack.pushInt(result ? 1 : 0);
		return Result.CONTINUE;
	}
}
