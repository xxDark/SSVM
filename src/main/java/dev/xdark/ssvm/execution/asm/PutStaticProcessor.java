package dev.xdark.ssvm.execution.asm;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.util.VMHelper;
import dev.xdark.ssvm.value.Value;
import org.objectweb.asm.tree.FieldInsnNode;

/**
 * Stores static field value.
 *
 * @author xDark
 */
public final class PutStaticProcessor implements InstructionProcessor<FieldInsnNode> {

	@Override
	public Result execute(FieldInsnNode insn, ExecutionContext ctx) {
		VirtualMachine vm = ctx.getVM();
		VMHelper helper = vm.getHelper();
		InstanceJavaClass owner = (InstanceJavaClass) helper.findClass(ctx.getOwner().getClassLoader(), insn.owner, true);
		Value value = ctx.getStack().popGeneric();
		if (!owner.setFieldValue(insn.name, insn.desc, value)) {
			helper.throwException(vm.getSymbols().java_lang_NoSuchFieldError, insn.name);
		}
		return Result.CONTINUE;
	}
}
