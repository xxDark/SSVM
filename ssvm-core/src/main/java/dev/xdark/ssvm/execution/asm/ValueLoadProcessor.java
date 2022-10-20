package dev.xdark.ssvm.execution.asm;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;
import org.objectweb.asm.tree.VarInsnNode;

/**
 * Loads value from local variable.
 *
 * @author xDark
 */
public final class ValueLoadProcessor implements InstructionProcessor<VarInsnNode> {

	@Override
	public Result execute(VarInsnNode insn, ExecutionContext<?> ctx) {
		ctx.getStack().pushReference(ctx.getLocals().loadReference(insn.var));
		return Result.CONTINUE;
	}
}
