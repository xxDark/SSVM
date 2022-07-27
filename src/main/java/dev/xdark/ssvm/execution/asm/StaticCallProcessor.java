package dev.xdark.ssvm.execution.asm;

import dev.xdark.ssvm.asm.VMCallInsnNode;
import dev.xdark.ssvm.asm.VMOpcodes;
import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.util.AsmUtil;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;

/**
 * Invokes static method.
 *
 * @author xDark
 */
public final class StaticCallProcessor extends CallProcessor {

	@Override
	public Result execute(MethodInsnNode insn, ExecutionContext ctx) {
		if (AsmUtil.isValid(insn)) {
			InsnList list = ctx.getMethod().getNode().instructions;
			list.set(insn, new VMCallInsnNode(insn, VMOpcodes.VM_INVOKESTATIC));
		}
		ctx.setInsnPosition(ctx.getInsnPosition() - 1);
		return Result.CONTINUE;
	}
}
