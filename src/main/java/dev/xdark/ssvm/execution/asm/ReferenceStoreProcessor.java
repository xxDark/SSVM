package dev.xdark.ssvm.execution.asm;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.value.ObjectValue;
import org.objectweb.asm.tree.VarInsnNode;

/**
 * Stores value into a local variable.
 *
 * @author xDark
 */
public final class ReferenceStoreProcessor implements InstructionProcessor<VarInsnNode> {

	@Override
	public Result execute(VarInsnNode insn, ExecutionContext<?> ctx) {
		ObjectValue ref = ctx.getStack().popReference();
		ctx.getLocals().setReference(insn.var, ref);
		return Result.CONTINUE;
	}
}
