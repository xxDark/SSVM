package dev.xdark.ssvm.execution.asm;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Locals;
import dev.xdark.ssvm.execution.Result;
import org.objectweb.asm.tree.IincInsnNode;

/**
 * Increments local variable.
 *
 * @author xDark
 */
public final class VariableIncrementProcessor implements InstructionProcessor<IincInsnNode> {

	@Override
	public Result execute(IincInsnNode insn, ExecutionContext ctx) {
		Locals locals = ctx.getLocals();
		int idx = insn.var;
		locals.setInt(idx, locals.loadInt(idx) + insn.incr);
		return Result.CONTINUE;
	}
}
