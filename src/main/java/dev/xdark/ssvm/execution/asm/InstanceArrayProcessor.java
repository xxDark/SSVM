package dev.xdark.ssvm.execution.asm;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;
import lombok.val;
import org.objectweb.asm.tree.TypeInsnNode;

/**
 * Allocates new array of objects.
 *
 * @author xDark
 */
public final class InstanceArrayProcessor implements InstructionProcessor<TypeInsnNode> {

	@Override
	public Result execute(TypeInsnNode insn, ExecutionContext ctx) {
		val stack = ctx.getStack();
		val length = stack.pop().asInt();
		val vm = ctx.getVM();
		val helper = vm.getHelper();
		val klass = helper.findClass(ctx.getOwner().getClassLoader(), insn.desc, true);
		if (klass == null) {
			helper.throwException(vm.getSymbols().java_lang_NoClassDefFoundError, insn.desc);
		}
		helper.checkArrayLength(length);
		stack.push(helper.newArray(klass, length));
		return Result.CONTINUE;
	}
}
