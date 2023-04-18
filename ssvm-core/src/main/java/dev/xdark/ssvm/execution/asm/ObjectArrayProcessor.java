package dev.xdark.ssvm.execution.asm;

import dev.xdark.ssvm.asm.VMOpcodes;
import dev.xdark.ssvm.asm.VMTypeInsnNode;
import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.mirror.type.JavaClass;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.TypeInsnNode;

/**
 * Allocates new array of objects.
 *
 * @author xDark
 */
public final class ObjectArrayProcessor implements InstructionProcessor<TypeInsnNode> {

	@Override
	public Result execute(TypeInsnNode insn, ExecutionContext<?> ctx) {
		JavaClass type = ctx.getOperations().findClass(ctx.getOwner(), insn.desc, false);
		VMTypeInsnNode wrapper = new VMTypeInsnNode(insn, VMOpcodes.VM_REFERENCE_NEW_ARRAY, type);
		InsnList list = ctx.getMethod().getNode().instructions;
		list.set(insn, wrapper);
		ctx.setInsnPosition(ctx.getInsnPosition() - 1);
		return Result.CONTINUE;
	}
}
