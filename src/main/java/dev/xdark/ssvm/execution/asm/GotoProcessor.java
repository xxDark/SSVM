package dev.xdark.ssvm.execution.asm;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.util.AsmUtil;
import org.objectweb.asm.tree.JumpInsnNode;

/**
 * Jumps to the label.
 *
 * @author xDark
 */
public final class GotoProcessor implements InstructionProcessor<JumpInsnNode> {

	@Override
	public Result execute(JumpInsnNode insn, ExecutionContext ctx) {
		ctx.setInsnPosition(AsmUtil.getIndex(insn.label));
		return Result.CONTINUE;
	}
}
