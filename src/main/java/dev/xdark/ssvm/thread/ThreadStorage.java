package dev.xdark.ssvm.thread;

import dev.xdark.ssvm.execution.Locals;
import dev.xdark.ssvm.execution.Stack;
import dev.xdark.ssvm.mirror.JavaMethod;
import dev.xdark.ssvm.value.Value;

/**
 * Thread storage.
 *
 * @author xDark
 */
public interface ThreadStorage extends Iterable<Value> {

	/**
	 * Returns an array slice of data.
	 *
	 * @param size Array size.
	 * @return array slice.
	 */
	ThreadRegion push(int size);

	/**
	 * Decreases current index.
	 *
	 * @param size Decrease count.
	 */
	void pop(int size);

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

	default Stack newStack(JavaMethod method) {
		return newStack(method.getMaxStack());
	}

	default Locals newLocals(JavaMethod method) {
		return newLocals(method.getMaxLocals());
	}
}
