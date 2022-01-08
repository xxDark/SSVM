package dev.xdark.ssvm;

import dev.xdark.ssvm.execution.ExecutionContext;

import java.util.Deque;

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
	Deque<ExecutionContext> getBacktrace();
}
