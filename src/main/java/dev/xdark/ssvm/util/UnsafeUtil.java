package dev.xdark.ssvm.util;

import lombok.experimental.UtilityClass;
import sun.misc.Unsafe;

import java.lang.reflect.Field;

/**
 * Unsafe utilities.
 *
 * @author xDark
 */
@UtilityClass
public class UnsafeUtil {

	private final Unsafe UNSAFE;
	public final int ARRAY_BYTE_BASE_OFFSET;

	/**
	 * @return unsafe instance.
	 */
	public Unsafe get() {
		return UNSAFE;
	}

	static {
		try {
			Unsafe unsafe = null;
			for (Field field : Unsafe.class.getDeclaredFields()) {
				if (Unsafe.class == field.getType()) {
					field.setAccessible(true);
					unsafe = (Unsafe) field.get(null);
					break;
				}
			}
			if (unsafe == null) {
				throw new IllegalStateException("Unable to locate unsafe instance");
			}
			ARRAY_BYTE_BASE_OFFSET = unsafe.arrayBaseOffset(byte[].class);
			UNSAFE = unsafe;
		} catch (IllegalAccessException ex) {
			throw new ExceptionInInitializerError(ex);
		}
	}
}
