package dev.xdark.ssvm.execution;

import dev.xdark.ssvm.asm.NewInsnNode;
import dev.xdark.ssvm.jit.JitHelper;

/**
 * Fast path for NEW instruction.
 *
 * @author xDark
 */
public final class VMNewProcessor implements InstructionProcessor<NewInsnNode> {

	@Override
	public Result execute(NewInsnNode insn, ExecutionContext ctx) {
		ctx.getStack().push(JitHelper.allocateInstance(insn.getJavaType(), ctx));
		return Result.CONTINUE;
	}
}
