package dev.xdark.ssvm.execution.asm;

import dev.xdark.ssvm.asm.LinkedDynamicCallNode;
import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.util.InvokeDynamicLinker;
import lombok.val;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;

/**
 * Links invokedynamic calls.
 *
 * @author xDark
 */
public final class InvokeDynamicLinkerProcessor implements InstructionProcessor<InvokeDynamicInsnNode> {

	@Override
	public Result execute(InvokeDynamicInsnNode insn, ExecutionContext ctx) {
		val linked = InvokeDynamicLinker.linkCall(insn, ctx);
		// Rewrite instruction
		val list = ctx.getMethod().getNode().instructions;
		list.set(insn, new LinkedDynamicCallNode(insn, linked));
		// Move insn position backwards so that VM visits
		// us yet again.
		ctx.setInsnPosition(ctx.getInsnPosition() - 1);
		return Result.CONTINUE;
	}
}
