package dev.xdark.ssvm.util;

import lombok.experimental.UtilityClass;

import java.io.Closeable;

/**
 * Dispsoe helpers.
 *
 * @author xDark
 */
@UtilityClass
public class CloseableUtil {

	/**
	 * Calls {@link Closeable#close()}.
	 *
	 * @param o Object to call close on.
	 */
	public void close(Object o) {
		if (o instanceof Closeable) {
			try {
				((Closeable) o).close();
			} catch (Exception ignored) {
			}
		}
	}
}
