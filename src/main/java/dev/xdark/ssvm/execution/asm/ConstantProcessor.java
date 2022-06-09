package dev.xdark.ssvm.execution.asm;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.value.Value;
import org.objectweb.asm.tree.AbstractInsnNode;

/**
 * Pushes generic constant onto the stack.
 *
 * @author xDark
 */
public final class ConstantProcessor implements InstructionProcessor<AbstractInsnNode> {

	private final Value cst;

	/**
	 * @param cst Constant to push.
	 */
	public ConstantProcessor(Value cst) {
		this.cst = cst;
	}

	@Override
	public Result execute(AbstractInsnNode insn, ExecutionContext ctx) {
		ctx.getStack().pushGeneric(cst);
		return Result.CONTINUE;
	}
}
