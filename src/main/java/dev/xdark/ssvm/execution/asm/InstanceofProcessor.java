package dev.xdark.ssvm.execution.asm;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.value.IntValue;
import dev.xdark.ssvm.value.ObjectValue;
import lombok.val;
import org.objectweb.asm.tree.TypeInsnNode;

/**
 * Performs instanceof check.
 */
public final class InstanceofProcessor implements InstructionProcessor<TypeInsnNode> {

	@Override
	public Result execute(TypeInsnNode insn, ExecutionContext ctx) {
		val vm = ctx.getVM();
		val javaClass = vm.getHelper().findClass(ctx.getOwner().getClassLoader(), insn.desc, false);
		if (javaClass instanceof InstanceJavaClass) ((InstanceJavaClass) javaClass).loadClassesWithoutMarkingResolved();
		val stack = ctx.getStack();
		val value = stack.<ObjectValue>pop();
		if (value.isNull()) {
			stack.push(IntValue.ZERO);
		} else {
			stack.push(javaClass.isAssignableFrom(value.getJavaClass()) ? IntValue.ONE : IntValue.ZERO);
		}
		return Result.CONTINUE;
	}
}
