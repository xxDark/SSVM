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
	private final long STRING_CHARS_OFFSET;

	/**
	 * @return unsafe instance.
	 */
	public Unsafe get() {
		return UNSAFE;
	}

	/**
	 * @return {@code true} if {@link #getChars(String)}
	 * can be used to get string characters directly.
	 */
	public boolean stringValueFieldAccessible() {
		return STRING_CHARS_OFFSET != -1;
	}

	/**
	 * @param str String to get characters from.
	 * @return string characters.
	 */
	public char[] getChars(String str) {
		long offset = STRING_CHARS_OFFSET;
		return offset == -1 ? str.toCharArray() : (char[]) UNSAFE.getObject(str, offset);
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
			long charsOffset = -1;
			try {
				Field field = String.class.getDeclaredField("value");
				if (char[].class == field.getType()) {
					charsOffset = unsafe.objectFieldOffset(field);
				}
			} catch (NoSuchFieldException ignored) {
			}
			STRING_CHARS_OFFSET = charsOffset;
			UNSAFE = unsafe;
		} catch (IllegalAccessException ex) {
			throw new ExceptionInInitializerError(ex);
		}
	}
}
