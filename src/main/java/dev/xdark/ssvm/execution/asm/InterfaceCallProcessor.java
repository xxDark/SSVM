package dev.xdark.ssvm.execution.asm;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.jit.JitHelper;
import org.objectweb.asm.tree.MethodInsnNode;

/**
 * Invokes interface method.
 *
 * @author xDark
 */
public final class InterfaceCallProcessor implements InstructionProcessor<MethodInsnNode> {

	@Override
	public Result execute(MethodInsnNode insn, ExecutionContext ctx) {
		JitHelper.invokeInterface(insn.owner, insn.name, insn.desc, ctx);
		return Result.CONTINUE;
	}
}
