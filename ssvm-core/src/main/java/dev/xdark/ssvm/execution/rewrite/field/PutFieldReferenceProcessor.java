package dev.xdark.ssvm.execution.rewrite.field;

import dev.xdark.ssvm.asm.VMFieldInsnNode;
import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.execution.Stack;
import dev.xdark.ssvm.mirror.member.JavaField;
import dev.xdark.ssvm.mirror.type.InstanceClass;
import dev.xdark.ssvm.value.InstanceValue;
import dev.xdark.ssvm.value.ObjectValue;
import org.objectweb.asm.tree.FieldInsnNode;

/**
 * Fast path processor for PUTFIELD.
 *
 * @author xDark
 */
public final class PutFieldReferenceProcessor implements InstructionProcessor<VMFieldInsnNode> {

	@Override
	public Result execute(VMFieldInsnNode insn, ExecutionContext<?> ctx) {
		Stack stack = ctx.getStack();
		ObjectValue value = stack.popReference();
		InstanceValue instance = ctx.getOperations().checkNotNull(stack.popReference());
		JavaField field = insn.getResolved();
		ctx.getMemoryManager().writeValue(instance, field.getOffset(), value);
		return Result.CONTINUE;
	}
}
