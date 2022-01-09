package dev.xdark.ssvm.execution.asm;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.value.IntValue;
import dev.xdark.ssvm.value.ObjectValue;
import org.objectweb.asm.tree.TypeInsnNode;

/**
 * Performs instanceof check.
 */
public final class InstanceofProcessor implements InstructionProcessor<TypeInsnNode> {

	@Override
	public Result execute(TypeInsnNode insn, ExecutionContext ctx) {
		var vm = ctx.getVM();
		var javaClass = vm.findClass(ctx.getOwner().getClassLoader(), insn.desc, true);
		var stack = ctx.getStack();
		var value = stack.<ObjectValue>pop();
		stack.push(new IntValue(javaClass.isAssignableFrom(value.getJavaClass()) ? 1 : 0));
		return Result.CONTINUE;
	}
}
