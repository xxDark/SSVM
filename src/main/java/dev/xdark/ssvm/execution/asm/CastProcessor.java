package dev.xdark.ssvm.execution.asm;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.value.ObjectValue;
import org.objectweb.asm.tree.TypeInsnNode;

/**
 * Casts value.
 *
 * @author xDark
 */
public final class CastProcessor implements InstructionProcessor<TypeInsnNode> {

	@Override
	public Result execute(TypeInsnNode insn, ExecutionContext ctx) {
		var vm = ctx.getVM();
		var desc = insn.desc;
		var type = vm.findClass(ctx.getOwner().getClassLoader(), desc, true);
		var against = ctx.getStack().<ObjectValue>peek().getJavaClass();
		if (!type.isAssignableFrom(against)) {
			vm.getHelper().throwException(vm.getSymbols().java_lang_ClassCastException, against.getName() + " cannot be cast to " + type.getName());
		}
		return Result.CONTINUE;
	}
}
