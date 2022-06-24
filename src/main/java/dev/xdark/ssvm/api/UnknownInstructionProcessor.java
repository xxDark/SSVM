package dev.xdark.ssvm.api;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.execution.PanicException;
import dev.xdark.ssvm.execution.Result;
import org.objectweb.asm.tree.AbstractInsnNode;

/**
 * Processor for unknown instructions.
 *
 * @author xDark
 */
final class UnknownInstructionProcessor implements InstructionProcessor<AbstractInsnNode> {
	UnknownInstructionProcessor() {
	}

	@Override
	public Result execute(AbstractInsnNode insn, ExecutionContext ctx) {
		throw new PanicException("Unknown instruction: " + insn.getOpcode() + '/' + insn);
	}
}
