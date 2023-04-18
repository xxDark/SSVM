package dev.xdark.ssvm.execution.rewrite;

import dev.xdark.ssvm.asm.VMFieldInsnNode;
import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.memory.management.MemoryManager;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.mirror.JavaField;

/**
 * Fast path processor for GETSTATIC.
 *
 * @author xDark
 */
public final class GetStaticFloatProcessor implements InstructionProcessor<VMFieldInsnNode> {

	@Override
	public Result execute(VMFieldInsnNode insn, ExecutionContext<?> ctx) {
		JavaField field = insn.getResolved();
		InstanceJavaClass klass = field.getOwner();
		MemoryManager memory = ctx.getMemoryManager();
		float value = Float.floatToRawIntBits(klass.getOop().getData().readInt(memory.getStaticOffset(klass) + field.getOffset()));
		ctx.getStack().pushFloat(value);
		return Result.CONTINUE;
	}
}
