package dev.xdark.ssvm.util;

import lombok.experimental.UtilityClass;
import sun.misc.Unsafe;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

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
	private final long STRING_BYTES_OFFSET;
	private final MethodHandle NEW_STRING_FROM_CHARS;

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
		return STRING_CHARS_OFFSET != -1L;
	}

	/**
	 * @param str String to get characters from.
	 * @return string characters.
	 */
	public char[] getChars(String str) {
		long offset = STRING_CHARS_OFFSET;
		return offset == -1L ? str.toCharArray() : (char[]) UNSAFE.getObject(str, offset);
	}

	/**
	 * @param str String to get bytes from.
	 * @return string bytes.
	 */
	public byte[] getBytes(String str) {
		long offset = STRING_BYTES_OFFSET;
		return offset == -1L ? str.getBytes() : (byte[]) UNSAFE.getObject(str, offset);
	}

	/**
	 * @param chars Character array to create string from.
	 * @return new string.
	 */
	public String newString(char[] chars) {
		if (NEW_STRING_FROM_CHARS != null) {
			try {
				return (String) NEW_STRING_FROM_CHARS.invokeExact(chars);
			} catch (Throwable ex) {
				throw new RuntimeException(ex);
			}
		}
		return new String(chars);
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
			long bytesOffset = -1;
			try {
				Field field = String.class.getDeclaredField("value");
				if (char[].class == field.getType()) {
					charsOffset = unsafe.objectFieldOffset(field);
				} else if (byte[].class == field.getType()) {
					bytesOffset = unsafe.objectFieldOffset(field);
				}
			} catch (NoSuchFieldException ignored) {
			}
			STRING_CHARS_OFFSET = charsOffset;
			STRING_BYTES_OFFSET = bytesOffset;
			MethodHandle newString;
			try {
				Class<?> sharedSecrets = Class.forName("sun.misc.SharedSecrets");
				Method m = sharedSecrets.getMethod("getJavaLangAccess");
				Object jla = m.invoke(null);
				m = jla.getClass().getDeclaredMethod("newStringUnsafe", char[].class);
				m.setAccessible(true);
				newString = MethodHandles.lookup().unreflect(m).bindTo(jla);
			} catch (ClassNotFoundException | InvocationTargetException | NoSuchMethodException e) {
				newString = null;
			}
			NEW_STRING_FROM_CHARS = newString;
			UNSAFE = unsafe;
		} catch (IllegalAccessException ex) {
			throw new ExceptionInInitializerError(ex);
		}
	}
}
