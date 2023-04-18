package dev.xdark.ssvm.operation;

import dev.xdark.ssvm.value.ObjectValue;

/**
 * VM primitive operations.
 *
 * @author xDark
 */
public interface PrimitiveOperations {

	/**
	 * Unwraps long value.
	 *
	 * @param value Wrapper to unwrap.
	 * @return primitive value.
	 */
	long unboxLong(ObjectValue value);

	/**
	 * Unwraps double value.
	 *
	 * @param value Wrapper to unwrap.
	 * @return primitive value.
	 */
	double unboxDouble(ObjectValue value);

	/**
	 * Unwraps int value.
	 *
	 * @param value Wrapper to unwrap.
	 * @return primitive value.
	 */
	int unboxInt(ObjectValue value);

	/**
	 * Unwraps float value.
	 *
	 * @param value Wrapper to unwrap.
	 * @return primitive value.
	 */
	float unboxFloat(ObjectValue value);

	/**
	 * Unwraps char value.
	 *
	 * @param value Wrapper to unwrap.
	 * @return primitive value.
	 */
	char unboxChar(ObjectValue value);

	/**
	 * Unwraps short value.
	 *
	 * @param value Wrapper to unwrap.
	 * @return primitive value.
	 */
	short unboxShort(ObjectValue value);

	/**
	 * Unwraps byte value.
	 *
	 * @param value Wrapper to unwrap.
	 * @return primitive value.
	 */
	byte unboxByte(ObjectValue value);

	/**
	 * Unwraps boolean value.
	 *
	 * @param value Wrapper to unwrap.
	 * @return primitive value.
	 */
	boolean unboxBoolean(ObjectValue value);

	/**
	 * Boxes long value.
	 *
	 * @param value Value to box.
	 * @return boxed value.
	 */
	ObjectValue boxLong(long value);

	/**
	 * Boxes double value.
	 *
	 * @param value Value to box.
	 * @return boxed value.
	 */
	ObjectValue boxDouble(double value);

	/**
	 * Boxes int value.
	 *
	 * @param value Value to box.
	 * @return boxed value.
	 */
	ObjectValue boxInt(int value);

	/**
	 * Boxes float value.
	 *
	 * @param value Value to box.
	 * @return boxed value.
	 */
	ObjectValue boxFloat(float value);

	/**
	 * Boxes char value.
	 *
	 * @param value Value to box.
	 * @return boxed value.
	 */
	ObjectValue boxChar(char value);

	/**
	 * Boxes short value.
	 *
	 * @param value Value to box.
	 * @return boxed value.
	 */
	ObjectValue boxShort(short value);

	/**
	 * Boxes byte value.
	 *
	 * @param value Value to box.
	 * @return boxed value.
	 */
	ObjectValue boxByte(byte value);

	/**
	 * Boxes boolean value.
	 *
	 * @param value Value to box.
	 * @return boxed value.
	 */
	ObjectValue boxBoolean(boolean value);
}
