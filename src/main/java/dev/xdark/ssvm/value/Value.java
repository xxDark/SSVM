package dev.xdark.ssvm.value;

/**
 * Represents VM internal value
 *
 * @author xDark
 */
public interface Value {

	/**
	 * Attempts to represent this value as {@code T}.
	 *
	 * @param <T>
	 * 		Type to represent.
	 *
	 * @return this value as {@code T}.
	 *
	 * @throws IllegalArgumentException
	 * 		if this value cannot be represented as {@code T}.
	 */
	<T> T as(Class<T> type);

	/**
	 * Shortcut for {@code as(long.class)}.
	 *
	 * @return this value as long.
	 *
	 * @see Value#as(Class)
	 */
	default long asLong() {
		return as(long.class);
	}

	/**
	 * Shortcut for {@code as(double.class)}.
	 *
	 * @return this value as double.
	 *
	 * @see Value#as(Class)
	 */
	default double asDouble() {
		return as(double.class);
	}

	/**
	 * Shortcut for {@code as(int.class)}.
	 *
	 * @return this value as int.
	 *
	 * @see Value#as(Class)
	 */
	default int asInt() {
		return as(int.class);
	}

	/**
	 * Shortcut for {@code as(float.class)}.
	 *
	 * @return this value as float.
	 *
	 * @see Value#as(Class)
	 */
	default float asFloat() {
		return as(float.class);
	}

	/**
	 * Shortcut for {@code as(char.class)}.
	 *
	 * @return this value as char.
	 *
	 * @see Value#as(Class)
	 */
	default char asChar() {
		return as(char.class);
	}

	/**
	 * Shortcut for {@code as(short.class)}.
	 *
	 * @return this value as short.
	 *
	 * @see Value#as(Class)
	 */
	default short asShort() {
		return as(short.class);
	}

	/**
	 * Shortcut for {@code as(byte.class)}.
	 *
	 * @return this value as byte.
	 *
	 * @see Value#as(Class)
	 */
	default byte asByte() {
		return as(byte.class);
	}

	/**
	 * Shortcut for {@code as(boolean.class)}.
	 *
	 * @return this value as boolean.
	 *
	 * @see Value#as(Class)
	 */
	default boolean asBoolean() {
		return as(boolean.class);
	}

	/**
	 * Returns whether this value is wide or not.
	 *
	 * @return {@code true} if the value is wide.
	 */
	boolean isWide();

	/**
	 * Returns whether this as uninitialized value or not.
	 *
	 * @return {@code true} if the value is uninitialized.
	 */
	boolean isUninitialized();

	/**
	 * Returns whether this value is null or not.
	 *
	 * @return {@code true} if the value is null.
	 */
	boolean isNull();

	/**
	 * Returns whether this value is void or not.
	 *
	 * @return {@code true} if the value is void.
	 */
	boolean isVoid();
}
