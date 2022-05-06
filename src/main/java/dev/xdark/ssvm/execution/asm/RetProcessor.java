package dev.xdark.ssvm.execution.asm;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.value.AddressValue;
import org.objectweb.asm.tree.VarInsnNode;

/**
 * Return from subroutine processor.
 *
 * @author xDark
 */
public final class RetProcessor implements InstructionProcessor<VarInsnNode> {

	@Override
	public Result execute(VarInsnNode insn, ExecutionContext ctx) {
		AddressValue v = ctx.getLocals().<AddressValue>load(insn.var);
		ctx.setInsnPosition(v.getPosition());
		return Result.CONTINUE;
	}
}
