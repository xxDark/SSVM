package dev.xdark.ssvm.execution.asm;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;
import org.objectweb.asm.tree.TypeInsnNode;

/**
 * Allocates new array of objects.
 *
 * @author xDark
 */
public final class InstanceArrayProcessor implements InstructionProcessor<TypeInsnNode> {

	@Override
	public Result execute(TypeInsnNode insn, ExecutionContext ctx) {
		var vm = ctx.getVM();
		var stack = ctx.getStack();
		var length = stack.pop().asInt();
		var arrayClass = vm.findClass(ctx.getOwner().getClassLoader(), insn.desc, true)
				.newArrayClass();
		stack.push(vm.getMemoryManager().newArray(arrayClass, length, 8L));
		return Result.CONTINUE;
	}
}
