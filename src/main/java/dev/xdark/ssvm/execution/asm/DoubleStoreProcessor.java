package dev.xdark.ssvm.execution.asm;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.value.TopValue;
import lombok.val;
import org.objectweb.asm.tree.VarInsnNode;

/**
 * Stores double into a local variable.
 *
 * @author xDark
 */
public final class DoubleStoreProcessor implements InstructionProcessor<VarInsnNode> {

	@Override
	public Result execute(VarInsnNode insn, ExecutionContext ctx) {
		val locals = ctx.getLocals();
		val var = insn.var;
		locals.set(var, ctx.getStack().popWide());
		locals.set(var + 1, TopValue.INSTANCE);
		return Result.CONTINUE;
	}
}
