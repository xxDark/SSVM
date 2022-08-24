package dev.xdark.ssvm.execution.rewrite;

import dev.xdark.ssvm.asm.VMFieldInsnNode;
import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.memory.management.MemoryManager;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.mirror.JavaField;

/**
 * Fast path processor for PUTSTATIC.
 *
 * @author xDark
 */
public final class PutStaticDoubleProcessor implements InstructionProcessor<VMFieldInsnNode> {

	@Override
	public Result execute(VMFieldInsnNode insn, ExecutionContext ctx) {
		JavaField field = insn.getResolved();
		InstanceJavaClass klass = field.getOwner();
		MemoryManager memory = ctx.getMemoryManager();
		klass.getOop().getData().writeLong(memory.getStaticOffset(klass) + field.getOffset(), Double.doubleToRawLongBits(ctx.getStack().popDouble()));
		return Result.CONTINUE;
	}
}
