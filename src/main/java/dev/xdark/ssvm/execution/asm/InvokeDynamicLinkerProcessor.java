package dev.xdark.ssvm.execution.asm;

import dev.xdark.ssvm.asm.LinkedDynamicCallNode;
import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.execution.SafePoint;
import dev.xdark.ssvm.value.InstanceValue;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;

/**
 * Links invokedynamic calls.
 *
 * @author xDark
 */
public final class InvokeDynamicLinkerProcessor implements InstructionProcessor<InvokeDynamicInsnNode> {

	@Override
	public Result execute(InvokeDynamicInsnNode insn, ExecutionContext ctx) {
		InstanceValue linked;
		SafePoint safePoint = ctx.getSafePoint();
		safePoint.increment();
		try {
			linked = ctx.getInvokeDynamicLinker().linkCall(insn, ctx.getOwner());
			ctx.getGarbageCollector().makeGlobalReference(linked);
		} finally {
			safePoint.decrement();
		}
		// Rewrite instruction
		InsnList list = ctx.getMethod().getNode().instructions;
		list.set(insn, new LinkedDynamicCallNode(insn, linked));
		// Move insn position backwards so that VM visits
		// us yet again.
		ctx.setInsnPosition(ctx.getInsnPosition() - 1);
		return Result.CONTINUE;
	}
}
