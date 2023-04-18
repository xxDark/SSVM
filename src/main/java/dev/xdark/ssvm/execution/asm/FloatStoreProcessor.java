package dev.xdark.ssvm.execution.asm;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;
import org.objectweb.asm.tree.VarInsnNode;

/**
 * Stores float into a local variable.
 *
 * @author xDark
 */
public final class FloatStoreProcessor implements InstructionProcessor<VarInsnNode> {

	@Override
	public Result execute(VarInsnNode insn, ExecutionContext<?> ctx) {
		ctx.getLocals().setFloat(insn.var, ctx.getStack().popFloat());
		return Result.CONTINUE;
	}
}
