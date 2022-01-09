package dev.xdark.ssvm.thread;

import dev.xdark.ssvm.execution.ExecutionContext;

import java.util.ArrayDeque;

/**
 * Thread backtrace.
 *
 * @author xDark
 */
public interface Backtrace extends Iterable<ExecutionContext> {

	/**
	 * Returns first frame.
	 *
	 * @return first frame.
	 */
	ExecutionContext first();

	/**
	 * Returns last frame.
	 *
	 * @return last frame.
	 */
	ExecutionContext last();

	/**
	 * Returns frame at the specific index.
	 *
	 * @param index
	 * 		Index of the frame.
	 *
	 * @return frame at the specific index.
	 */
	ExecutionContext get(int index);

	/**
	 * Returns backtrace depth.
	 *
	 * @return backtrace depth.
	 */
	int count();

	/**
	 * Pushes frame.
	 *
	 * @param ctx
	 * 		Context to push.
	 */
	void push(ExecutionContext ctx);

	/**
	 * Pops frame.
	 *
	 * @return popped frame.
	 */
	ExecutionContext pop();
}
