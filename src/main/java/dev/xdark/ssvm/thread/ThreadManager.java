package dev.xdark.ssvm.thread;

import dev.xdark.ssvm.value.InstanceValue;

/**
 * VM thread manager.
 *
 * @author xDark
 */
public interface ThreadManager {

	/**
	 * Attaches current thread.
	 * Does nothing if thread is already attached.
	 */
	void attachCurrentThread();

	/**
	 * Deatches current thread.
	 * Does nothing if thread is not attached.
	 */
	void detachCurrentThread();

	/**
	 * Returns VM thread from Java thread.
	 *
	 * @param thread Thread to get VM thread from.
	 * @return VM thread.
	 */
	VMThread getVmThread(Thread thread);

	/**
	 * Returns VMThread by an instance.
	 *
	 * @param thread VM thread oop.
	 */
	VMThread getVmThread(InstanceValue thread);

	/**
	 * @return array of threads.
	 */
	VMThread[] getThreads();

	/**
	 * @return array of visible threads.
	 */
	VMThread[] getVisibleThreads();

	/**
	 * Returns current VM thread.
	 *
	 * @return current VM thread.
	 */
	default VMThread currentThread() {
		return getVmThread(Thread.currentThread());
	}

	/**
	 * Creates main thread.
	 *
	 * @return main thread.
	 */
	VMThread createMainThread();

	/**
	 * Suspends all VM threads.
	 */
	void suspendAll();

	/**
	 * Resumes all VM threads.
	 */
	void resumeAll();

	/**
	 * Causes the currently executing thread to sleep.
	 *
	 * @param millis The length of time to sleep in milliseconds.
	 * @throws InterruptedException If any thread has interrupted the current thread.
	 */
	void sleep(long millis) throws InterruptedException;
}
