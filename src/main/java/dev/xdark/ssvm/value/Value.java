package dev.xdark.ssvm.value;

/**
 * Represents VM internal value
 *
 * @author xDark
 */
public interface Value {

	/**
	 * @return this value as long.
	 */
	default long asLong() {
		throw new UnsupportedOperationException();
	}

	/**
	 * @return this value as double.
	 */
	default double asDouble() {
		throw new UnsupportedOperationException();
	}

	/**
	 * @return this value as int.
	 */
	default int asInt() {
		throw new UnsupportedOperationException();
	}

	/**
	 * @return this value as float.
	 */
	default float asFloat() {
		throw new UnsupportedOperationException();
	}

	/**
	 * @return this value as char.
	 */
	default char asChar() {
		throw new UnsupportedOperationException();
	}

	/**
	 * @return this value as short.
	 */
	default short asShort() {
		throw new UnsupportedOperationException();
	}

	/**
	 * @return this value as byte.
	 */
	default byte asByte() {
		throw new UnsupportedOperationException();
	}

	/**
	 * @return this value as boolean.
	 */
	default boolean asBoolean() {
		throw new UnsupportedOperationException();
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
