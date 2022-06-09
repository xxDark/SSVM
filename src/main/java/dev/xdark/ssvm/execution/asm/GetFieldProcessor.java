package dev.xdark.ssvm.execution.asm;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.execution.Stack;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import org.objectweb.asm.tree.FieldInsnNode;

/**
 * Pushes field value of an object.
 *
 * @author xDark
 */
public final class GetFieldProcessor implements InstructionProcessor<FieldInsnNode> {

	@Override
	public Result execute(FieldInsnNode insn, ExecutionContext ctx) {
		InstanceJavaClass klass = (InstanceJavaClass) ctx.getHelper().tryFindClass(ctx.getClassLoader(), insn.owner, true);
		Stack stack = ctx.getStack();
		stack.pushGeneric(ctx.getOperations().getGenericField(stack.pop(), klass, insn.name, insn.desc));
		return Result.CONTINUE;
	}
}
