package dev.xdark.ssvm.execution;

/**
 * VM safepoint functionality.
 *
 * @author xDark
 */
public interface SafePoint {

	/**
	 * Polls safepoint.
	 */
	void poll();

	/**
	 * Requests a safepoint.
	 */
	void request();
}
