package dev.xdark.ssvm.execution;

/**
 * VM safepoint functionality.
 *
 * @author xDark
 */
public interface SafePoint {

	/**
	 * Polls safepoint.
	 *
	 * @return {@code true} if safepoint was polled
	 * and the thread must block.
	 */
	boolean poll();

	/**
	 * Polls safepoint.
	 * @return {@code true} if safepoint was polled
	 * and the thread must block.
	 */
	boolean pollAndSuspend();

	/**
	 * Requests a safepoint.
	 */
	void request();

	/**
	 * Finishes safepoint.
	 */
	void complete();
}
