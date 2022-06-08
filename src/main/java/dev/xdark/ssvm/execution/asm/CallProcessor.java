package dev.xdark.ssvm.execution.asm;

import dev.xdark.ssvm.execution.InstructionProcessor;
import dev.xdark.ssvm.value.Value;
import org.objectweb.asm.tree.MethodInsnNode;

/**
 * Abstract class for call processors.
 *
 * @author xDark
 */
abstract class CallProcessor implements InstructionProcessor<MethodInsnNode> {
	// TODO make all processors use VM specific instruction to cache argument types
	// Also merge locals creation in one method in attempt to use
	// thread regions directly
	protected static final Value[] NO_VALUES = {};
}
