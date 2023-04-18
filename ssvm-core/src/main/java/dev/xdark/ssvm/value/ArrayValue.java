package dev.xdark.ssvm.value;

/**
 * VM array value.
 *
 * @author xDark
 */
public interface ArrayValue extends ObjectValue {

	/**
	 * Returns long value at specific index.
	 *
	 * @param index Index to get long from.
	 * @return long value.
	 */
	@Deprecated
	long getLong(int index);

	/**
	 * Returns double value at specific index.
	 *
	 * @param index Index to get double from.
	 * @return double value.
	 */
	@Deprecated
	double getDouble(int index);

	/**
	 * Returns int value at specific index.
	 *
	 * @param index Index to get int from.
	 * @return double value.
	 */
	@Deprecated
	int getInt(int index);

	/**
	 * Returns float value at specific index.
	 *
	 * @param index Index to get float from.
	 * @return float value.
	 */
	@Deprecated
	float getFloat(int index);

	/**
	 * Returns char value at specific index.
	 *
	 * @param index Index to get char from.
	 * @return char value.
	 */
	@Deprecated
	char getChar(int index);

	/**
	 * Returns short value at specific index.
	 *
	 * @param index Index to get short from.
	 * @return short value.
	 */
	@Deprecated
	short getShort(int index);

	/**
	 * Returns byte value at specific index.
	 *
	 * @param index Index to get byte from.
	 * @return byte value.
	 */
	@Deprecated
	byte getByte(int index);

	/**
	 * Returns boolean value at specific index.
	 *
	 * @param index Index to get boolean from.
	 * @return boolean value.
	 */
	@Deprecated
	boolean getBoolean(int index);

	/**
	 * Returns VM value at specific index.
	 *
	 * @param index Index to get value from.
	 * @return VM value.
	 */
	@Deprecated
	ObjectValue getReference(int index);

	/**
	 * Sets long value of an array.
	 *
	 * @param index Index to store value to.
	 * @param value Value to set.
	 */
	@Deprecated
	void setLong(int index, long value);

	/**
	 * Sets double value of an array.
	 *
	 * @param index Index to store value to.
	 * @param value Value to set.
	 */
	@Deprecated
	void setDouble(int index, double value);

	/**
	 * Sets int value of an array.
	 *
	 * @param index Index to store value to.
	 * @param value Value to set.
	 */
	@Deprecated
	void setInt(int index, int value);

	/**
	 * Sets float value of an array.
	 *
	 * @param index Index to store value to.
	 * @param value Value to set.
	 */
	@Deprecated
	void setFloat(int index, float value);

	/**
	 * Sets char value of an array.
	 *
	 * @param index Index to store value to.
	 * @param value Value to set.
	 */
	@Deprecated
	void setChar(int index, char value);

	/**
	 * Sets short value of an array.
	 *
	 * @param index Index to store value to.
	 * @param value Value to set.
	 */
	@Deprecated
	void setShort(int index, short value);

	/**
	 * Sets byte value of an array.
	 *
	 * @param index Index to store value to.
	 * @param value Value to set.
	 */
	@Deprecated
	void setByte(int index, byte value);

	/**
	 * Sets boolean value of an array.
	 *
	 * @param index Index to store value to.
	 * @param value Value to set.
	 */
	@Deprecated
	void setBoolean(int index, boolean value);

	/**
	 * Sets VM value of an array.
	 *
	 * @param index Index to store value to.
	 * @param value Value to set.
	 */
	@Deprecated
	void setReference(int index, ObjectValue value);

	/**
	 * Returns array length.
	 *
	 * @return array length.
	 */
	@Deprecated
	int getLength();
}
