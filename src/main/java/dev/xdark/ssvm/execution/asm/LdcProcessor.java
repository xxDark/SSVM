package dev.xdark.ssvm.execution.asm;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.value.Value;
import lombok.val;
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
		Value value;
		val cst = insn.cst;
		if (cst instanceof Value) {
			// VM-patched.
			value = (Value) cst;
		} else {
			value = ctx.getHelper().valueFromLdc(cst);
		}
		ctx.getStack().pushGeneric(value);
		return Result.CONTINUE;
	}
}
