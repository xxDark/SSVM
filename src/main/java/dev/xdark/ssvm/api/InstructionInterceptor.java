package dev.xdark.ssvm.api;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.Result;
import org.objectweb.asm.tree.AbstractInsnNode;

/**
 * Instruction interceptor.
 * Does not work if code is JITted.
 *
 * @author xDark
 */
public interface InstructionInterceptor {

	/**
	 * @param ctx  Execution context.
	 * @param insn Instruction to intercept.
	 */
	Result intercept(ExecutionContext ctx, AbstractInsnNode insn);
}
