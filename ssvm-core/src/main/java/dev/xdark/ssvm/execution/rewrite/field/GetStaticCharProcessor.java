package dev.xdark.ssvm.execution.rewrite.field;

import dev.xdark.ssvm.asm.VMFieldInsnNode;
import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.mirror.member.JavaField;
import dev.xdark.ssvm.mirror.type.InstanceClass;

/**
 * Fast path processor for GETSTATIC.
 *
 * @author xDark
 */
public final class GetStaticCharProcessor implements InstructionProcessor<VMFieldInsnNode> {

	@Override
	public Result execute(VMFieldInsnNode insn, ExecutionContext<?> ctx) {
		JavaField field = insn.getResolved();
		InstanceClass klass = field.getOwner();
		char value = klass.getOop().getData().readChar(field.getOffset());
		ctx.getStack().pushInt(value);
		return Result.CONTINUE;
	}
}
