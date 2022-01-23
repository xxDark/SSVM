package dev.xdark.ssvm.execution.asm;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;
import org.objectweb.asm.tree.LdcInsnNode;

/**
 * Processor for {@link LdcInsnNode}.
 * <p>
 * Pushes generic ldc onto the stack.
 *
 * @author xDark
 */
public final class LdcProcessor implements InstructionProcessor<LdcInsnNode> {

	@Override
	public Result execute(LdcInsnNode insn, ExecutionContext ctx) {
		ctx.getStack().pushGeneric(ctx.getHelper().valueFromLdc(insn.cst));
		return Result.CONTINUE;
	}
}
