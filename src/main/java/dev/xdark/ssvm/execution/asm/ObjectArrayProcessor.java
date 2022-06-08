package dev.xdark.ssvm.execution.asm;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.execution.Stack;
import dev.xdark.ssvm.mirror.JavaClass;
import org.objectweb.asm.tree.TypeInsnNode;

/**
 * Allocates new array of objects.
 *
 * @author xDark
 */
public final class ObjectArrayProcessor implements InstructionProcessor<TypeInsnNode> {

	@Override
	public Result execute(TypeInsnNode insn, ExecutionContext ctx) {
		JavaClass type = ctx.getHelper().tryFindClass(ctx.getClassLoader(), insn.desc, true);
		Stack stack = ctx.getStack();
		int length = stack.popInt();
		stack.push(ctx.getOperations().allocateArray(type, length));
		return Result.CONTINUE;
	}
}
