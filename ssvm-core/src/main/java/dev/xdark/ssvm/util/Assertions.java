package dev.xdark.ssvm.util;

import dev.xdark.ssvm.execution.PanicException;

/**
 * Assertions.
 *
 * @author xDark
 */
public class Assertions {

	public static void check(boolean state, String message) {
		if (!state) {
			throw new PanicException(message);
		}
	}

	public static void notNull(Object v, String message) {
		check(v != null, message);
	}

	public static void isNull(Object v, String message) {
		check(v == null, message);
	}
}
