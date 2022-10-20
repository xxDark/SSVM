package dev.xdark.ssvm.execution.asm;

import dev.xdark.ssvm.asm.VMOpcodes;
import dev.xdark.ssvm.asm.VMTypeInsnNode;
import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.mirror.type.JavaClass;
import dev.xdark.ssvm.util.AsmUtil;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.TypeInsnNode;

/**
 * Casts value.
 *
 * @author xDark
 */
public final class CastProcessor implements InstructionProcessor<TypeInsnNode> {

	@Override
	public Result execute(TypeInsnNode insn, ExecutionContext<?> ctx) {
		String desc = AsmUtil.normalizeDescriptor(insn.desc);
		JavaClass type = ctx.getOperations().findClass(ctx.getOwner().getClassLoader(), desc, true);
		InsnList list = ctx.getMethod().getNode().instructions;
		list.set(insn, new VMTypeInsnNode(insn, VMOpcodes.VM_CHECKCAST, type));
		ctx.setInsnPosition(ctx.getInsnPosition() - 1);
		return Result.CONTINUE;
	}
}
