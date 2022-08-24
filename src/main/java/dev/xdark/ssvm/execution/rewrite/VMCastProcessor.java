package dev.xdark.ssvm.execution.rewrite;

import dev.xdark.ssvm.asm.VMTypeInsnNode;
import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;

/**
 * Fast path for CHECKCAST.
 */
public class VMCastProcessor implements InstructionProcessor<VMTypeInsnNode> {
	@Override
	public Result execute(VMTypeInsnNode insn, ExecutionContext<?> ctx) {
		ctx.getOperations().checkCast(ctx.getStack().peekReference(), insn.getJavaType());
		return Result.CONTINUE;
	}
}
