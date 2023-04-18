package dev.xdark.ssvm.operation;

import dev.xdark.ssvm.value.ObjectValue;

/**
 * VM synchronization operations.
 *
 * @author xDark
 */
public interface SynchronizationOperations {

	/**
	 * Attempts to lock object monitor.
	 * Throws VM exception if an object is {@code null}.
	 *
	 * @param value Object to lock.
	 */
	void monitorEnter(ObjectValue value);

	/**
	 * Attempts to unlock object monitor.
	 * Throws VM exception if an object is {@code null},
	 * or if current thread does not own the lock.
	 *
	 * @param value Object to unlock.
	 */
	void monitorExit(ObjectValue value);
}
