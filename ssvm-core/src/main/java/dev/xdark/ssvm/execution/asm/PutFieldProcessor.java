package dev.xdark.ssvm.execution.asm;

import dev.xdark.ssvm.asm.VMFieldInsnNode;
import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.execution.VMException;
import dev.xdark.ssvm.mirror.type.InstanceClass;
import dev.xdark.ssvm.mirror.member.JavaField;
import dev.xdark.ssvm.operation.VMOperations;
import dev.xdark.ssvm.util.AsmUtil;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;

import static dev.xdark.ssvm.asm.VMOpcodes.VM_PUTFIELD_BOOLEAN;
import static dev.xdark.ssvm.asm.VMOpcodes.VM_PUTFIELD_REFERENCE;
import static org.objectweb.asm.Type.ARRAY;

/**
 * Stores object field value.
 *
 * @author xDark
 */
public final class PutFieldProcessor implements InstructionProcessor<FieldInsnNode> {

	@Override
	public Result execute(FieldInsnNode insn, ExecutionContext<?> ctx) {
		if (AsmUtil.isValid(insn)) {
			VMOperations ops = ctx.getOperations();
			InstanceClass klass = (InstanceClass) ops.findClass(ctx.getOwner(), insn.owner, true);
			JavaField field = ctx.getLinkResolver().resolveVirtualField(klass, insn.name, insn.desc);
			int sort = field.getType().getSort();
			int opcode;
			if (sort >= ARRAY) {
				opcode = VM_PUTFIELD_REFERENCE;
			} else {
				opcode = VM_PUTFIELD_BOOLEAN + (sort - 1);
			}
			InsnList list = ctx.getMethod().getNode().instructions;
			list.set(insn, new VMFieldInsnNode(insn, opcode, field));
			ops.initialize(field.getOwner());
		}
		ctx.setInsnPosition(ctx.getInsnPosition() - 1);
		return Result.CONTINUE;
	}
}
