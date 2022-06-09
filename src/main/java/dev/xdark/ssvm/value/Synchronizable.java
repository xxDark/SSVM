package dev.xdark.ssvm.value;

/**
 * VM synchronization logic.
 *
 * @author xDark
 */
public interface Synchronizable {

	/**
	 * Locks the object.
	 */
	void monitorEnter();

	/**
	 * Unlocks the object.
	 */
	void monitorExit();

	/**
	 * Causes the current thread to wait until it is awakened,
	 * typically by being notified or interrupted.
	 *
	 * @param timeoutMillis The maximum time to wait, in milliseconds.
	 * @throws InterruptedException If Java thread was interrupted.
	 */
	void monitorWait(long timeoutMillis) throws InterruptedException;

	/**
	 * Wakes up a single thread that is waiting
	 * on this object's monitor.
	 */
	void monitorNotify();

	/**
	 * Wakes up all threads that are waiting
	 * on this object's monitor.
	 */
	void monitorNotifyAll();

	/**
	 * @return {@code true} if current thread holds this lock.
	 */
	boolean isHeldByCurrentThread();
}
