package dev.xdark.ssvm.execution.asm;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.value.FloatValue;
import dev.xdark.ssvm.value.Value;
import org.objectweb.asm.tree.AbstractInsnNode;

/**
 * Pushes float constant onto the stack.
 *
 * @author xDark
 */
public final class ConstantFloatProcessor implements InstructionProcessor<AbstractInsnNode> {

	private final Value cst;

	/**
	 * @param cst Float constant.
	 */
	public ConstantFloatProcessor(float cst) {
		this.cst = new FloatValue(cst);
	}

	@Override
	public Result execute(AbstractInsnNode insn, ExecutionContext ctx) {
		ctx.getStack().push(cst);
		return Result.CONTINUE;
	}
}
