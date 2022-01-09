package dev.xdark.ssvm.execution.asm;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.value.IntValue;
import org.objectweb.asm.tree.IincInsnNode;

/**
 * Increments local variable.
 *
 * @author xDark
 */
public final class VariableIncrementProcessor implements InstructionProcessor<IincInsnNode> {

	@Override
	public Result execute(IincInsnNode insn, ExecutionContext ctx) {
		var locals = ctx.getLocals();
		var idx = insn.var;
		locals.set(idx, new IntValue(locals.load(idx).asInt() + insn.incr));
		return Result.CONTINUE;
	}
}
