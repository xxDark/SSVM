package dev.xdark.ssvm.execution.asm;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;
import org.objectweb.asm.tree.AbstractInsnNode;

/**
 * Pushes double constant onto the stack.
 *
 * @author xDark
 */
public final class ConstantDoubleProcessor implements InstructionProcessor<AbstractInsnNode> {

	private final double cst;

	/**
	 * @param cst Double constant.
	 */
	public ConstantDoubleProcessor(double cst) {
		this.cst = cst;
	}

	@Override
	public Result execute(AbstractInsnNode insn, ExecutionContext ctx) {
		ctx.getStack().pushDouble(cst);
		return Result.CONTINUE;
	}
}
