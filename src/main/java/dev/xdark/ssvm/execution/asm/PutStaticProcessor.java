package dev.xdark.ssvm.execution.asm;

import dev.xdark.ssvm.asm.VMFieldInsnNode;
import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.mirror.JavaField;
import dev.xdark.ssvm.util.AsmUtil;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;

import static org.objectweb.asm.Type.*;
import static dev.xdark.ssvm.asm.VMOpcodes.*;

/**
 * Stores static field value.
 *
 * @author xDark
 */
public final class PutStaticProcessor implements InstructionProcessor<FieldInsnNode> {

	@Override
	public Result execute(FieldInsnNode insn, ExecutionContext<?> ctx) {
		if (AsmUtil.isValid(insn)) {
			InstanceJavaClass klass = (InstanceJavaClass) ctx.getHelper().tryFindClass(ctx.getClassLoader(), insn.owner, true);
			JavaField field = ctx.getLinkResolver().resolveStaticField(klass, insn.name, insn.desc);
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
	}
}
