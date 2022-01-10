package dev.xdark.ssvm.execution.asm;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.mirror.JavaClass;
import dev.xdark.ssvm.value.Value;
import org.objectweb.asm.tree.TypeInsnNode;

/**
 * Allocates new array of objects.
 *
 * @author xDark
 */
public final class InstanceArrayProcessor implements InstructionProcessor<TypeInsnNode> {

	@Override
	public Result execute(TypeInsnNode insn, ExecutionContext ctx) {
		var stack = ctx.getStack();
		var length = stack.pop().asInt();
		var vm = ctx.getVM();
		var helper = vm.getHelper();
		var klass = helper.findClass(ctx.getOwner().getClassLoader(), insn.desc, true);
		if (klass == null) {
			helper.throwException(vm.getSymbols().java_lang_NoClassDefFoundError, insn.desc);
		}
		helper.checkArrayLength(length);
		var arrayClass = klass.newArrayClass();
		var memoryManager = vm.getMemoryManager();
		stack.push(memoryManager.newArray(arrayClass, length, memoryManager.arrayIndexScale(Value.class)));
		return Result.CONTINUE;
	}
}
