package dev.xdark.ssvm.util;

import java.io.Closeable;

/**
 * Safe version of {@link Closeable} that
 * doesn't declare the exception on close method.
 *
 * @author xDark
 */
public interface SafeCloseable extends Closeable {

	@Override
	void close();
}
