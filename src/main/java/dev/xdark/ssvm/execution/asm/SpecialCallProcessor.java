package dev.xdark.ssvm.execution.asm;

import dev.xdark.ssvm.asm.VMCallInsnNode;
import dev.xdark.ssvm.asm.VMOpcodes;
import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.Result;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;

/**
 * Invokes exact method.
 *
 * @author xDark
 */
public final class SpecialCallProcessor extends CallProcessor {

	@Override
	public Result execute(MethodInsnNode insn, ExecutionContext ctx) {
		InsnList list = ctx.getMethod().getNode().instructions;
		list.set(insn, new VMCallInsnNode(insn, VMOpcodes.VM_INVOKESPECIAL));
		ctx.setInsnPosition(ctx.getInsnPosition() - 1);
		return Result.CONTINUE;
	}
}
