package dev.xdark.ssvm.execution.asm;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.jit.JitHelper;
import org.objectweb.asm.tree.TypeInsnNode;

/**
 * Allocates new instance.
 *
 * @author xDark
 */
public final class NewProcessor implements InstructionProcessor<TypeInsnNode> {

	@Override
	public Result execute(TypeInsnNode insn, ExecutionContext ctx) {
		JitHelper.allocateInstance(insn.desc, ctx);
		return Result.CONTINUE;
	}
}
