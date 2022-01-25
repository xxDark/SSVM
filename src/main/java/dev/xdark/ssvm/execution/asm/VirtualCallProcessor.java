package dev.xdark.ssvm.execution.asm;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.jit.JitHelper;
import org.objectweb.asm.tree.MethodInsnNode;

/**
 * Invokes virtual method.
 *
 * @author xDark
 */
public final class VirtualCallProcessor implements InstructionProcessor<MethodInsnNode> {

	@Override
	public Result execute(MethodInsnNode insn, ExecutionContext ctx) {
		JitHelper.invokeVirtual(insn.owner, insn.name, insn.desc, ctx);
		return Result.CONTINUE;
	}
}
