package dev.xdark.ssvm.util;

import lombok.experimental.UtilityClass;

/**
 * Dispsoe helpers.
 *
 * @author xDark
 */
@UtilityClass
public class DisposeUtil {

	/**
	 * Disposes an object if it is marked
	 * as {@link Disposable}.
	 *
	 * @param o Object to dispose.
	 */
	public void dispose(Object o) {
		if (o instanceof Disposable) {
			((Disposable) o).dispose();
		}
	}
}
