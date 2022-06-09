package dev.xdark.ssvm.execution;

/**
 * VM safepoint functionality.
 *
 * @author xDark
 */
public interface SafePoint {

	/**
	 * Increments the amount of
	 * {@literal unsafe} points.
	 */
	void increment();

	/**
	 * Decrements the amount of
	 * {@literal unsafe} points.
	 */
	void decrement();

	/**
	 * Attempts to increase the amount of
	 * unsafe points by {@literal 1} if there is none.
	 *
	 * @return {@code true} if the amount of unsafe
	 * points was incremented.
	 */
	boolean tryLock();
}
