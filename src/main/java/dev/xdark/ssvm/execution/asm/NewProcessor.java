package dev.xdark.ssvm.execution.asm;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.value.InstanceValue;
import lombok.val;
import org.objectweb.asm.tree.TypeInsnNode;

/**
 * Allocates new instance.
 *
 * @author xDark
 */
public final class NewProcessor implements InstructionProcessor<TypeInsnNode> {

	@Override
	public Result execute(TypeInsnNode insn, ExecutionContext ctx) {
		val vm = ctx.getVM();
		val helper = vm.getHelper();
		val type = helper.findClass(ctx.getOwner().getClassLoader(), insn.desc, true);
		if (type == null) {
			helper.throwException(vm.getSymbols().java_lang_NoClassDefFoundError, insn.desc);
		}
		val instance = vm.getMemoryManager().newInstance((InstanceJavaClass) type);
		helper.initializeDefaultValues(instance);
		ctx.getStack().push(instance);
		return Result.CONTINUE;
	}
}
