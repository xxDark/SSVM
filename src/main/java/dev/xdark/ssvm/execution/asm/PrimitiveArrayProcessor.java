package dev.xdark.ssvm.execution.asm;

import dev.xdark.ssvm.asm.DelegatingInsnNode;
import dev.xdark.ssvm.asm.VMOpcodes;
import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.IntInsnNode;

/**
 * Allocates new array.
 *
 * @author xDark
 */
public final class PrimitiveArrayProcessor implements InstructionProcessor<IntInsnNode> {

	@Override
	public Result execute(IntInsnNode insn, ExecutionContext<?> ctx) {
		int operand = insn.operand;
		int virtualOpcode = VMOpcodes.VM_BOOLEAN_NEW_ARRAY + (operand - Opcodes.T_BOOLEAN);
		DelegatingInsnNode<IntInsnNode> wrapper = new DelegatingInsnNode<>(insn, virtualOpcode);
		InsnList list = ctx.getMethod().getNode().instructions;
		list.set(insn, wrapper);
		ctx.setInsnPosition(ctx.getInsnPosition() - 1);
		return Result.CONTINUE;
	}
}
