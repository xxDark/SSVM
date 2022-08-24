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
	long getLong(int index);

	/**
	 * Returns double value at specific index.
	 *
	 * @param index Index to get double from.
	 * @return double value.
	 */
	double getDouble(int index);

	/**
	 * Returns int value at specific index.
	 *
	 * @param index Index to get int from.
	 * @return double value.
	 */
	int getInt(int index);

	/**
	 * Returns float value at specific index.
	 *
	 * @param index Index to get float from.
	 * @return float value.
	 */
	float getFloat(int index);

	/**
	 * Returns char value at specific index.
	 *
	 * @param index Index to get char from.
	 * @return char value.
	 */
	char getChar(int index);

	/**
	 * Returns short value at specific index.
	 *
	 * @param index Index to get short from.
	 * @return short value.
	 */
	short getShort(int index);

	/**
	 * Returns byte value at specific index.
	 *
	 * @param index Index to get byte from.
	 * @return byte value.
	 */
	byte getByte(int index);

	/**
	 * Returns boolean value at specific index.
	 *
	 * @param index Index to get boolean from.
	 * @return boolean value.
	 */
	boolean getBoolean(int index);

	/**
	 * Returns VM value at specific index.
	 *
	 * @param index Index to get value from.
	 * @return VM value.
	 */
	ObjectValue getValue(int index);

	/**
	 * Sets long value of an array.
	 *
	 * @param index Index to store value to.
	 * @param value Value to set.
	 */
	void setLong(int index, long value);

	/**
	 * Sets double value of an array.
	 *
	 * @param index Index to store value to.
	 * @param value Value to set.
	 */
	void setDouble(int index, double value);

	/**
	 * Sets int value of an array.
	 *
	 * @param index Index to store value to.
	 * @param value Value to set.
	 */
	void setInt(int index, int value);

	/**
	 * Sets float value of an array.
	 *
	 * @param index Index to store value to.
	 * @param value Value to set.
	 */
	void setFloat(int index, float value);

	/**
	 * Sets char value of an array.
	 *
	 * @param index Index to store value to.
	 * @param value Value to set.
	 */
	void setChar(int index, char value);

	/**
	 * Sets short value of an array.
	 *
	 * @param index Index to store value to.
	 * @param value Value to set.
	 */
	void setShort(int index, short value);

	/**
	 * Sets byte value of an array.
	 *
	 * @param index Index to store value to.
	 * @param value Value to set.
	 */
	void setByte(int index, byte value);

	/**
	 * Sets boolean value of an array.
	 *
	 * @param index Index to store value to.
	 * @param value Value to set.
	 */
	void setBoolean(int index, boolean value);

	/**
	 * Sets VM value of an array.
	 *
	 * @param index Index to store value to.
	 * @param value Value to set.
	 */
	void setReference(int index, ObjectValue value);

	/**
	 * Returns array length.
	 *
	 * @return array length.
	 */
	int getLength();
}
