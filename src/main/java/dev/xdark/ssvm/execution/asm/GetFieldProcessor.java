package dev.xdark.ssvm.execution.asm;

import dev.xdark.ssvm.asm.VMFieldInsnNode;
import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.execution.VMException;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.mirror.JavaField;
import dev.xdark.ssvm.util.AsmUtil;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;

import static dev.xdark.ssvm.asm.VMOpcodes.VM_GETFIELD_BOOLEAN;
import static dev.xdark.ssvm.asm.VMOpcodes.VM_GETFIELD_REFERENCE;
import static org.objectweb.asm.Type.ARRAY;

/**
 * Pushes field value of an object.
 *
 * @author xDark
 */
public final class GetFieldProcessor implements InstructionProcessor<FieldInsnNode> {

	@Override
	public Result execute(FieldInsnNode insn, ExecutionContext<?> ctx) {
		if (AsmUtil.isValid(insn)) {
			InstanceJavaClass klass = (InstanceJavaClass) ctx.getHelper().tryFindClass(ctx.getClassLoader(), insn.owner, true);
			JavaField field;
			int sort;
			try {
				field = ctx.getLinkResolver().resolveVirtualField(klass, insn.name, insn.desc);
				sort = field.getType().getSort();
			} catch (VMException ex) {
				sort = Type.getType(insn.desc).getSort();
				field = null;
			}
			int opcode;
			if (sort >= ARRAY) {
				opcode = VM_GETFIELD_REFERENCE;
			} else {
				opcode = VM_GETFIELD_BOOLEAN + (sort - 1);
			}
			InsnList list = ctx.getMethod().getNode().instructions;
			list.set(insn, new VMFieldInsnNode(insn, opcode, field));
			if (field != null) {
				field.getOwner().initialize();
			}
		}
		ctx.setInsnPosition(ctx.getInsnPosition() - 1);
		return Result.CONTINUE;
	}
}
