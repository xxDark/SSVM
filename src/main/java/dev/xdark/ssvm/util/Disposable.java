package dev.xdark.ssvm.util;

/**
 * Disposable object marker.
 *
 * @author xDark
 */
public interface Disposable extends AutoCloseable {

	void dispose();

	@Override
	default void close() {
		dispose();
	}
}
