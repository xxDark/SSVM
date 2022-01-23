package dev.xdark.ssvm.execution.asm;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.value.VoidValue;
import org.objectweb.asm.tree.AbstractInsnNode;

/**
 * Stops execution.
 *
 * @author xDark
 */
public final class ReturnVoidProcessor implements InstructionProcessor<AbstractInsnNode> {

	@Override
	public Result execute(AbstractInsnNode insn, ExecutionContext ctx) {
		ctx.setResult(VoidValue.INSTANCE);
		return Result.ABORT;
	}
}
