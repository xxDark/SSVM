package dev.xdark.ssvm.value;

import dev.xdark.ssvm.memory.Memory;
import dev.xdark.ssvm.mirror.JavaClass;

/**
 * Represents VM object value.
 *
 * @author xDark
 */
public interface ObjectValue extends Value {

	/**
	 * Returns object class.
	 *
	 * @return object class.
	 */
	JavaClass getJavaClass();

	/**
	 * Returns object data.
	 *
	 * @return object data.
	 */
	Memory getMemory();

	/**
	 * Locks monitor.
	 */
	void monitorEnter();

	/**
	 * Unlocks monitor.
	 */
	void monitorExit();

	/**
	 * Causes the current thread to wait until it is awakened,
	 * typically by being notified or interrupted.
	 *
	 * @param timeoutMillis
	 * 		The maximum time to wait, in milliseconds.
	 *
	 * @throws InterruptedException
	 * 		If Java thread was interrupted.
	 */
	void vmWait(long timeoutMillis) throws InterruptedException;

	/**
	 * Wakes up a single thread that is waiting
	 * on this object's monitor.
	 */
	void vmNotify();

	/**
	 * Wakes up all threads that are waiting
	 * on this object's monitor.
	 */
	void vmNotifyAll();

	/**
	 * @return {@code true} if current thread holds this lock.
	 */
	boolean isHeldByCurrentThread();
}
