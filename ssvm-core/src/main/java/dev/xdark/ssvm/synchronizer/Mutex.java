package dev.xdark.ssvm.synchronizer;

/**
 * Mutex.
 *
 * @author xDark
 */
public interface Mutex {

	/**
	 * @return Mutex id.
	 */
	int id();

	/**
	 * Locks mutex.
	 */
	void lock();

	/**
	 * Unlocks mutex.
	 */
	boolean tryUnlock();

	/**
	 * Causes the current thread to wait until it is awakened,
	 * typically by being notified or interrupted.
	 *
	 * @param timeoutMillis The maximum time to wait, in milliseconds.
	 * @throws InterruptedException If Java thread was interrupted.
	 */
	void doWait(long timeoutMillis) throws InterruptedException;

	/**
	 * Wakes up a single thread that is waiting
	 * on this object's monitor.
	 */
	void doNotify();

	/**
	 * Wakes up all threads that are waiting
	 * on this object's monitor.
	 */
	void doNotifyAll();

	/**
	 * @return {@code true} if current thread holds this lock.
	 */
	boolean isHeldByCurrentThread();
}
