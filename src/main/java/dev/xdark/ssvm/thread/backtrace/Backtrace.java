package dev.xdark.ssvm.thread.backtrace;

/**
 * Thread backtrace.
 *
 * @author xDark
 */
public interface Backtrace extends Iterable<StackFrame> {

	/**
	 * Returns first frame.
	 *
	 * @return first frame.
	 */
	StackFrame first();

	/**
	 * Returns last frame.
	 *
	 * @return last frame.
	 */
	StackFrame last();

	/**
	 * Returns frame at the specific index.
	 *
	 * @param index Index of the frame.
	 * @return frame at the specific index.
	 */
	StackFrame get(int index);

	/**
	 * Returns backtrace depth.
	 *
	 * @return backtrace depth.
	 */
	int count();

	/**
	 * Pushes frame.
	 *
	 * @param frame Frame to push.
	 */
	void push(StackFrame frame);

	/**
	 * Pops frame.
	 *
	 * @return popped frame.
	 */
	StackFrame pop();

	/**
	 * Copies backtrace.
	 *
	 * @return copied backtrace.
	 */
	Backtrace copy();
}
