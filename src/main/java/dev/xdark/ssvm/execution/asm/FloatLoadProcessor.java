package dev.xdark.ssvm.execution.asm;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;
import org.objectweb.asm.tree.VarInsnNode;

/**
 * Loads float from local variable.
 *
 * @author xDark
 */
public final class FloatLoadProcessor implements InstructionProcessor<VarInsnNode> {

	@Override
	public Result execute(VarInsnNode insn, ExecutionContext ctx) {
		ctx.getStack().push(ctx.getLocals().load(insn.var));
		return Result.CONTINUE;
	}
}
