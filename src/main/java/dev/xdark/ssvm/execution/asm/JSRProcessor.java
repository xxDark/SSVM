package dev.xdark.ssvm.execution.asm;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.util.AsmUtil;
import org.objectweb.asm.tree.JumpInsnNode;

/**
 * Jump subroutine processor.
 *
 * @author xDark
 */
public final class JSRProcessor implements InstructionProcessor<JumpInsnNode> {

	@Override
	public Result execute(JumpInsnNode insn, ExecutionContext ctx) {
		ctx.getStack().pushInt(ctx.getInsnPosition());
		ctx.setInsnPosition(AsmUtil.getIndex(insn.label));
		return Result.CONTINUE;
	}
}
