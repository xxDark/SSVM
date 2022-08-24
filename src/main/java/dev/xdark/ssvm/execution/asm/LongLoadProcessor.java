package dev.xdark.ssvm.execution.asm;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;
import org.objectweb.asm.tree.VarInsnNode;

/**
 * Loads long from local variable.
 *
 * @author xDark
 */
public final class LongLoadProcessor implements InstructionProcessor<VarInsnNode> {

	@Override
	public Result execute(VarInsnNode insn, ExecutionContext<?> ctx) {
		ctx.getStack().pushLong(ctx.getLocals().loadLong(insn.var));
		return Result.CONTINUE;
	}
}
