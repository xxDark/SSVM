package dev.xdark.ssvm.execution.asm;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.jit.JitHelper;
import org.objectweb.asm.tree.TypeInsnNode;

/**
 * Casts value.
 *
 * @author xDark
 */
public final class CastProcessor implements InstructionProcessor<TypeInsnNode> {

	@Override
	public Result execute(TypeInsnNode insn, ExecutionContext ctx) {
		JitHelper.checkCast(insn.desc, ctx);
		return Result.CONTINUE;
	}
}
