package dev.xdark.ssvm.execution;

import dev.xdark.ssvm.asm.NewInsnNode;

/**
 * Fast path for NEW instruction.
 *
 * @author xDark
 */
public final class VMNewProcessor implements InstructionProcessor<NewInsnNode> {

	@Override
	public Result execute(NewInsnNode insn, ExecutionContext ctx) {
		ctx.getStack().push(ctx.getOperations().allocateInstance(insn.getJavaType()));
		return Result.CONTINUE;
	}
}
