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
		return Double.longBitsToDouble(asLong());
	}

	/**
	 * @return this value as int.
	 */
	default int asInt() {
		return (int) asLong();
	}

	/**
	 * @return this value as float.
	 */
	default float asFloat() {
		return Float.intBitsToFloat(asInt());
	}

	/**
	 * @return this value as char.
	 */
	default char asChar() {
		return (char) asInt();
	}

	/**
	 * @return this value as short.
	 */
	default short asShort() {
		return (short) asInt();
	}

	/**
	 * @return this value as byte.
	 */
	default byte asByte() {
		return (byte) asInt();
	}

	/**
	 * @return this value as boolean.
	 */
	default boolean asBoolean() {
		return asInt() != 0;
	}

	/**
	 * Returns whether this value is wide or not.
	 *
	 * @return {@code true} if the value is wide.
	 */
	boolean isWide();

	/**
	 * Returns whether this value is void or not.
	 *
	 * @return {@code true} if the value is void.
	 */
	boolean isVoid();
}
