package dev.xdark.ssvm.execution.asm;

import dev.xdark.ssvm.asm.NewInsnNode;
import dev.xdark.ssvm.asm.VMLdcInsnNode;
import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.jit.JitHelper;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.TypeInsnNode;

/**
 * Allocates new instance.
 *
 * @author xDark
 */
public final class NewProcessor implements InstructionProcessor<TypeInsnNode> {

	@Override
	public Result execute(TypeInsnNode insn, ExecutionContext ctx) {
		String desc = insn.desc;
		InstanceJavaClass klass = (InstanceJavaClass) ctx.getHelper().findClass(ctx.getOwner().getClassLoader(), desc, true);
		InsnList list = ctx.getMethod().getNode().instructions;
		list.set(insn, new NewInsnNode(insn, klass));
		ctx.setInsnPosition(ctx.getInsnPosition() - 1);
		return Result.CONTINUE;
	}
}
