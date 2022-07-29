package dev.xdark.ssvm.execution.asm;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import org.objectweb.asm.tree.FieldInsnNode;

/**
 * Stores static field value.
 *
 * @author xDark
 */
public final class PutStaticProcessor implements InstructionProcessor<FieldInsnNode> {

	@Override
	public Result execute(FieldInsnNode insn, ExecutionContext ctx) {
		/*
				InstanceJavaClass klass = (InstanceJavaClass) ctx.getHelper().tryFindClass(ctx.getClassLoader(), insn.owner, true);
		JavaField field = ctx.getLinkResolver().resolveStaticField(klass, insn.name, insn.desc);
		if (AsmUtil.isValid(insn)) {
			int sort = field.getType().getSort();
			int opcode;
			if (sort >= ARRAY) {
				opcode = VM_PUTSTATIC_REFERENCE;
			} else {
				opcode = VM_PUTSTATIC_BOOLEAN + (sort - 1);
			}
			InsnList list = ctx.getMethod().getNode().instructions;
			list.set(insn, new VMFieldInsnNode(insn, opcode, field));
			field.getOwner().initialize();
		}
		ctx.setInsnPosition(ctx.getInsnPosition() - 1);
		return Result.CONTINUE;
		 */
		InstanceJavaClass klass = (InstanceJavaClass) ctx.getHelper().tryFindClass(ctx.getClassLoader(), insn.owner, true);
		ctx.getOperations().putGeneric(klass, insn.name, insn.desc, ctx.getStack().popGeneric());
		return Result.CONTINUE;
	}
}
