package dev.xdark.ssvm.execution.asm;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.value.ObjectValue;
import org.objectweb.asm.tree.AbstractInsnNode;

/**
 * Pushes generic constant onto the stack.
 *
 * @author xDark
 */
public final class ConstantReferenceProcessor implements InstructionProcessor<AbstractInsnNode> {

	private final ObjectValue cst;

	/**
	 * @param cst Constant to push.
	 */
	public ConstantReferenceProcessor(ObjectValue cst) {
		this.cst = cst;
	}

	@Override
	public Result execute(AbstractInsnNode insn, ExecutionContext<?> ctx) {
		ctx.getStack().pushReference(cst);
		return Result.CONTINUE;
	}
}
