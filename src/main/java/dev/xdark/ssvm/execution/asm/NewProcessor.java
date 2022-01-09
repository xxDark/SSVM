package dev.xdark.ssvm.execution.asm;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import org.objectweb.asm.tree.TypeInsnNode;

/**
 * Allocates new instance.
 *
 * @author xDark
 */
public final class NewProcessor implements InstructionProcessor<TypeInsnNode> {

	@Override
	public Result execute(TypeInsnNode insn, ExecutionContext ctx) {
		var vm = ctx.getVM();
		var type = vm.findClass(ctx.getOwner().getClassLoader(), insn.desc, true);
		ctx.getStack().push(vm.getMemoryManager().newInstance((InstanceJavaClass) type));
		return Result.CONTINUE;
	}
}
