package dev.xdark.ssvm.execution.asm;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;

/**
 * Links invokedynamic calls.
 *
 * @author xDark
 */
public final class InvokeDynamicLinkerProcessor implements InstructionProcessor<InvokeDynamicInsnNode> {

	@Override
	public Result execute(InvokeDynamicInsnNode insn, ExecutionContext ctx) {
		var vm = ctx.getVM();
		vm.getHelper().throwException(vm.getSymbols().java_lang_UnsatisfiedLinkError, "Linker is not implemented yet");
		return Result.CONTINUE;
	}
}
