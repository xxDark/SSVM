package dev.xdark.ssvm.execution.asm;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.value.IntValue;
import lombok.val;
import org.objectweb.asm.tree.IincInsnNode;

/**
 * Increments local variable.
 *
 * @author xDark
 */
public final class VariableIncrementProcessor implements InstructionProcessor<IincInsnNode> {

	@Override
	public Result execute(IincInsnNode insn, ExecutionContext ctx) {
		val locals = ctx.getLocals();
		int idx = insn.var;
		locals.set(idx, IntValue.of(locals.load(idx).asInt() + insn.incr));
		return Result.CONTINUE;
	}
}
