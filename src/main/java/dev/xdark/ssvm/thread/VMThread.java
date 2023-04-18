package dev.xdark.ssvm.thread;

import dev.xdark.ssvm.thread.backtrace.Backtrace;
import dev.xdark.ssvm.value.InstanceValue;

/**
 * Represents attached native Java thread.
 *
 * @author xDark
 */
public interface VMThread {

	/**
	 * Returns Java thread.
	 *
	 * @return Java thread.
	 */
	Thread getJavaThread();

	/**
	 * Returns thread backtrace.
	 *
	 * @return thread backtrace.
	 */
	Backtrace getBacktrace();

	/**
	 * Returns VM thread oop.
	 *
	 * @return thread oop.
	 */
	InstanceValue getOop();

	/**
	 * Sets native thread priority.
	 *
	 * @param priority Priority to set.
	 */
	void setPriority(int priority);

	/**
	 * Sets native thread name.
	 *
	 * @param name Name to set.
	 */
	void setName(String name);

	/**
	 * Starts native thread.
	 */
	void start();

	/**
	 * Interrupts native thread.
	 */
	void interrupt();

	/**
	 * Returns whether thread is alive or not.
	 *
	 * @return {@code true} if thread is alive,
	 * {@code false} otherwise.
	 */
	boolean isAlive();

	/**
	 * Tests if some Thread has been interrupted.
	 * The interrupted state is reset or not based on
	 * the value of clear that is passed.
	 *
	 * @param clear Whether interruption status
	 *              must be cleared.
	 * @return {@code true} if thread was interrupted,
	 * {@code false} otherwise.
	 */
	boolean isInterrupted(boolean clear);

	/**
	 * @return thread storage.
	 */
	ThreadStorage getThreadStorage();

	/**
	 * Suspends this thread.
	 */
	void suspend();

	/**
	 * Resumes this thread.
	 */
	void resume();

	/**
	 * Causes the currently executing thread to sleep.
	 *
	 * @param millis The length of time to sleep in milliseconds.
	 * @throws InterruptedException If any thread has interrupted the current thread.
	 */
	void sleep(long millis) throws InterruptedException;
}
