package dev.xdark.ssvm.thread;

import dev.xdark.ssvm.execution.Locals;
import dev.xdark.ssvm.execution.Stack;
import dev.xdark.ssvm.mirror.member.JavaMethod;

/**
 * Thread storage.
 *
 * @author xDark
 */
public interface ThreadStorage {

	/**
	 * Creates new stack.
	 *
	 * @param size Stack size.
	 * @return new stack.
	 */
	Stack newStack(int size);

	/**
	 * Creates new locals.
	 *
	 * @param size Locals size.
	 * @return new locals.
	 */
	Locals newLocals(int size);

	/**
	 * Creates new stack.
	 *
	 * @param method Method to create stack for.
	 * @return new stack.
	 */
	default Stack newStack(JavaMethod method) {
		return newStack(method.getMaxStack());
	}

	/**
	 * Creates new locals.
	 *
	 * @param method Method to create locals for.
	 * @return new locals.
	 */
	default Locals newLocals(JavaMethod method) {
		return newLocals(method.getMaxLocals());
	}

	/**
	 * Deallocates thread memory.
	 */
	void free();
}
