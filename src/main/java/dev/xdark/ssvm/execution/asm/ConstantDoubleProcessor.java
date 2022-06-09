package dev.xdark.ssvm.execution.asm;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.value.DoubleValue;
import dev.xdark.ssvm.value.Value;
import org.objectweb.asm.tree.AbstractInsnNode;

/**
 * Pushes double constant onto the stack.
 *
 * @author xDark
 */
public final class ConstantDoubleProcessor implements InstructionProcessor<AbstractInsnNode> {

	private final Value cst;

	/**
	 * @param cst Double constant.
	 */
	public ConstantDoubleProcessor(double cst) {
		this.cst = new DoubleValue(cst);
	}

	@Override
	public Result execute(AbstractInsnNode insn, ExecutionContext ctx) {
		ctx.getStack().pushWide(cst);
		return Result.CONTINUE;
	}
}
