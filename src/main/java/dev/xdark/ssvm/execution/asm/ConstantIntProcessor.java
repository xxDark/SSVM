package dev.xdark.ssvm.execution.asm;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.value.IntValue;
import dev.xdark.ssvm.value.Value;
import org.objectweb.asm.tree.AbstractInsnNode;

/**
 * Pushes int constant onto the stack.
 *
 * @author xDark
 */
public final class ConstantIntProcessor implements InstructionProcessor<AbstractInsnNode> {

	private final Value cst;

	/**
	 * @param cst
	 * 		Int constant.
	 */
	public ConstantIntProcessor(int cst) {
		this.cst = new IntValue(cst);
	}

	@Override
	public Result execute(AbstractInsnNode insn, ExecutionContext ctx) {
		ctx.getStack().push(cst);
		return Result.CONTINUE;
	}
}
