package dev.xdark.ssvm.util;

/**
 * Try with resources for a lock.
 *
 * @author xDark
 */
public interface AutoCloseableLock extends AutoCloseable {
	@Override
	void close();
}
