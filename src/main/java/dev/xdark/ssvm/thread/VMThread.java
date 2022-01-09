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
}
