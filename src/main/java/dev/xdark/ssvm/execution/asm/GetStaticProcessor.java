package dev.xdark.ssvm.execution.asm;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import org.objectweb.asm.tree.FieldInsnNode;

/**
 * Pushes static field value.
 *
 * @author xDark
 */
public final class GetStaticProcessor implements InstructionProcessor<FieldInsnNode> {

	@Override
	public Result execute(FieldInsnNode insn, ExecutionContext ctx) {
		var vm = ctx.getVM();
		var owner = (InstanceJavaClass) vm.findClass(ctx.getOwner().getClassLoader(), insn.owner, true);
		var value = owner.getStaticValue(insn.name, insn.desc);
		if (value == null) {
			throw new IllegalStateException("No such field: " + owner.getInternalName() + '.' + insn.name + insn.desc);
		}
		ctx.getStack().pushGeneric(value);
		return Result.CONTINUE;
	}
}
