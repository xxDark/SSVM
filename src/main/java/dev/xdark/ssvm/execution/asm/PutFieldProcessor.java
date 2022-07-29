package dev.xdark.ssvm.execution.asm;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.execution.Stack;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.value.ObjectValue;
import dev.xdark.ssvm.value.Value;
import org.objectweb.asm.tree.FieldInsnNode;

/**
 * Stores object field value.
 *
 * @author xDark
 */
public final class PutFieldProcessor implements InstructionProcessor<FieldInsnNode> {

	@Override
	public Result execute(FieldInsnNode insn, ExecutionContext ctx) {
		Stack stack = ctx.getStack();
		Value value = stack.popGeneric();
		ObjectValue instance = stack.pop();
		InstanceJavaClass klass = (InstanceJavaClass) ctx.getHelper().tryFindClass(ctx.getClassLoader(), insn.owner, true);
		ctx.getOperations().putGeneric(instance, klass, insn.name, insn.desc, value);
		return Result.CONTINUE;
	}
}
