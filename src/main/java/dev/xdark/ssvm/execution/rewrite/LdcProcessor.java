package dev.xdark.ssvm.execution.rewrite;

import dev.xdark.ssvm.asm.VMLdcInsnNode;
import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;

/**
 * Fast path for LDC instruction.
 *
 * @author xDark
 */
public final class LdcProcessor implements InstructionProcessor<VMLdcInsnNode> {

	@Override
	public Result execute(VMLdcInsnNode insn, ExecutionContext ctx) {
		ctx.getStack().pushGeneric(insn.getValue());
		return Result.CONTINUE;
	}
}
