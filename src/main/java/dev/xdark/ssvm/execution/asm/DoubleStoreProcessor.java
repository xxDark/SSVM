package dev.xdark.ssvm.execution.asm;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;
import org.objectweb.asm.tree.VarInsnNode;

/**
 * Stores double into a local variable.
 *
 * @author xDark
 */
public final class DoubleStoreProcessor implements InstructionProcessor<VarInsnNode> {

	@Override
	public Result execute(VarInsnNode insn, ExecutionContext ctx) {
		ctx.getLocals().set(insn.var, ctx.getStack().popWide());
		return Result.CONTINUE;
	}
}
