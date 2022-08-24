package dev.xdark.ssvm.execution.asm;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Locals;
import dev.xdark.ssvm.execution.Result;
import org.objectweb.asm.tree.VarInsnNode;

/**
 * Stores double into a local variable.
 *
 * @author xDark
 */
public final class DoubleStoreProcessor implements InstructionProcessor<VarInsnNode> {

	@Override
	public Result execute(VarInsnNode insn, ExecutionContext<?> ctx) {
		Locals locals = ctx.getLocals();
		int var = insn.var;
		locals.setDouble(var, ctx.getStack().popDouble());
		return Result.CONTINUE;
	}
}
