package dev.xdark.ssvm.execution;

import org.objectweb.asm.tree.AbstractInsnNode;

/**
 * ASM instruction processor.
 *
 * @param <I> Type of the instruction.
 */
public interface InstructionProcessor<I extends AbstractInsnNode> {

	/**
	 * Processes the instruction.
	 *
	 * @param insn Instruction to process.
	 * @param ctx  Execution context.
	 * @return the result of the execution.
	 */
	Result execute(I insn, ExecutionContext ctx);
}
