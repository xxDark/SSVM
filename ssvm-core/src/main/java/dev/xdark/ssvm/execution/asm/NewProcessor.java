package dev.xdark.ssvm.execution.asm;

import dev.xdark.ssvm.asm.VMOpcodes;
import dev.xdark.ssvm.asm.VMTypeInsnNode;
import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.mirror.type.InstanceClass;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.TypeInsnNode;

/**
 * Allocates new instance.
 *
 * @author xDark
 */
public final class NewProcessor implements InstructionProcessor<TypeInsnNode> {

	@Override
	public Result execute(TypeInsnNode insn, ExecutionContext<?> ctx) {
		String desc = insn.desc;
		InstanceClass klass = (InstanceClass) ctx.getOperations().findClass(ctx.getOwner(), desc, true);
		InsnList list = ctx.getMethod().getNode().instructions;
		list.set(insn, new VMTypeInsnNode(insn, VMOpcodes.VM_NEW, klass));
		ctx.setInsnPosition(ctx.getInsnPosition() - 1);
		return Result.CONTINUE;
	}
}
