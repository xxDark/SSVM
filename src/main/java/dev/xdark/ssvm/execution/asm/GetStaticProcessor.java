package dev.xdark.ssvm.execution.asm;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import lombok.val;
import org.objectweb.asm.tree.FieldInsnNode;

/**
 * Pushes static field value.
 *
 * @author xDark
 */
public final class GetStaticProcessor implements InstructionProcessor<FieldInsnNode> {

	@Override
	public Result execute(FieldInsnNode insn, ExecutionContext ctx) {
		val vm = ctx.getVM();
		val helper = vm.getHelper();
		InstanceJavaClass owner = (InstanceJavaClass) helper.findClass(ctx.getOwner().getClassLoader(), insn.owner, true);
		if (owner == null) {
			helper.throwException(vm.getSymbols().java_lang_ClassNotFoundException, insn.owner);
		}
		while (owner != null) {
			val value = owner.getStaticValue(insn.name, insn.desc);
			if (value != null) {
				ctx.getStack().pushGeneric(value);
				return Result.CONTINUE;
			}
			owner = owner.getSuperClass();
		}
		helper.throwException(vm.getSymbols().java_lang_NoSuchFieldError, insn.owner + '.' + insn.name + insn.desc);
		return Result.ABORT;
	}
}
