package dev.xdark.ssvm.execution.asm;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.value.LongValue;
import dev.xdark.ssvm.value.Value;
import org.objectweb.asm.tree.AbstractInsnNode;

/**
 * Pushes long constant onto the stack.
 *
 * @author xDark
 */
public final class ConstantLongProcessor implements InstructionProcessor<AbstractInsnNode> {

	private final Value cst;

	/**
	 * @param cst Long constant.
	 */
	public ConstantLongProcessor(long cst) {
		this.cst = LongValue.of(cst);
	}

	@Override
	public Result execute(AbstractInsnNode insn, ExecutionContext ctx) {
		ctx.getStack().pushWide(cst);
		return Result.CONTINUE;
	}
}
