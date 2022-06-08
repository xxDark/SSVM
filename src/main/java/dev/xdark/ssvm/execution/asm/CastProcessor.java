package dev.xdark.ssvm.execution.asm;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.mirror.JavaClass;
import dev.xdark.ssvm.util.AsmUtil;
import org.objectweb.asm.tree.TypeInsnNode;

/**
 * Casts value.
 *
 * @author xDark
 */
public final class CastProcessor implements InstructionProcessor<TypeInsnNode> {

	@Override
	public Result execute(TypeInsnNode insn, ExecutionContext ctx) {
		String desc = AsmUtil.normalizeDescriptor(insn.desc);
		JavaClass type = ctx.getHelper().tryFindClass(ctx.getClassLoader(), desc, true);
		ctx.getOperations().checkCast(ctx.getStack().peek(), type);
		return Result.CONTINUE;
	}
}
