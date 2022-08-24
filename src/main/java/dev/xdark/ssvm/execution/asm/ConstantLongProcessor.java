package dev.xdark.ssvm.execution.asm;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;
import org.objectweb.asm.tree.AbstractInsnNode;

/**
 * Pushes long constant onto the stack.
 *
 * @author xDark
 */
public final class ConstantLongProcessor implements InstructionProcessor<AbstractInsnNode> {

	private final long cst;

	/**
	 * @param cst Long constant.
	 */
	public ConstantLongProcessor(long cst) {
		this.cst = cst;
	}

	@Override
	public Result execute(AbstractInsnNode insn, ExecutionContext<?> ctx) {
		ctx.getStack().pushLong(cst);
		return Result.CONTINUE;
	}
}
