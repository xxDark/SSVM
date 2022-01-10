package dev.xdark.ssvm.thread;

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
	 * @param priority
	 * 		Priority to set.
	 */
	void setPriority(int priority);

	/**
	 * Sets native thread name.
	 *
	 * @param name
	 * 		Name to set.
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
}
