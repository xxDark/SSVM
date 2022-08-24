package dev.xdark.ssvm.execution.asm;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Locals;
import dev.xdark.ssvm.execution.Result;
import org.objectweb.asm.tree.VarInsnNode;

/**
 * Stores long into a local variable.
 *
 * @author xDark
 */
public final class LongStoreProcessor implements InstructionProcessor<VarInsnNode> {

	@Override
	public Result execute(VarInsnNode insn, ExecutionContext ctx) {
		Locals locals = ctx.getLocals();
		int var = insn.var;
		locals.setLong(var, ctx.getStack().popLong());
		return Result.CONTINUE;
	}
}
