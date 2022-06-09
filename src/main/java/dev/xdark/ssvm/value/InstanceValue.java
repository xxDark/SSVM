package dev.xdark.ssvm.value;

import dev.xdark.ssvm.mirror.InstanceJavaClass;

/**
 * Represents instance value.
 * (Arrays are represent differently).
 *
 * @author xDark
 */
public interface InstanceValue extends ObjectValue {

	@Override
	InstanceJavaClass getJavaClass();

	/**
	 * Returns long value of a field.
	 *
	 * @param field Field name.
	 * @return long value.
	 */
	long getLong(String field);

	/**
	 * Returns double value of a field.
	 *
	 * @param field Field name.
	 * @return double value.
	 */
	double getDouble(String field);

	/**
	 * Returns int value of a field.
	 *
	 * @param field Field name.
	 * @return int value.
	 */
	int getInt(String field);

	/**
	 * Returns float value of a field.
	 *
	 * @param field Field name.
	 * @return float value.
	 */
	float getFloat(String field);

	/**
	 * Returns char value of a field.
	 *
	 * @param field Field name.
	 * @return char value.
	 */
	char getChar(String field);

	/**
	 * Returns short value of a field.
	 *
	 * @param field Field name.
	 * @return short value.
	 */
	short getShort(String field);

	/**
	 * Returns byte value of a field.
	 *
	 * @param field Field name.
	 * @return byte value.
	 */
	byte getByte(String field);

	/**
	 * Returns boolean value of a field.
	 *
	 * @param field Field name.
	 * @return boolean value.
	 */
	boolean getBoolean(String field);

	/**
	 * Returns VM value of a field.
	 *
	 * @param field Field name.
	 * @return VM value.
	 */
	ObjectValue getValue(String field, String desc);

	/**
	 * Sets long value of a field.
	 *
	 * @param field field name.
	 * @param value Value to set.
	 */
	void setLong(String field, long value);

	/**
	 * Sets double value of a field.
	 *
	 * @param field field name.
	 * @param value Value to set.
	 */
	void setDouble(String field, double value);

	/**
	 * Sets int value of a field.
	 *
	 * @param field field name.
	 * @param value Value to set.
	 */
	void setInt(String field, int value);

	/**
	 * Sets float value of a field.
	 *
	 * @param field field name.
	 * @param value Value to set.
	 */
	void setFloat(String field, float value);

	/**
	 * Sets char value of a field.
	 *
	 * @param field field name.
	 * @param value Value to set.
	 */
	void setChar(String field, char value);

	/**
	 * Sets short value of a field.
	 *
	 * @param field field name.
	 * @param value Value to set.
	 */
	void setShort(String field, short value);

	/**
	 * Sets byte value of a field.
	 *
	 * @param field field name.
	 * @param value Value to set.
	 */
	void setByte(String field, byte value);

	/**
	 * Sets boolean value of a field.
	 *
	 * @param field field name.
	 * @param value Value to set.
	 */
	void setBoolean(String field, boolean value);

	/**
	 * Sets VM value of a field.
	 *
	 * @param field field name.
	 * @param desc  Field descriptor.
	 * @param value Value to set.
	 */
	void setValue(String field, String desc, ObjectValue value);

	/**
	 * Marks this object as initialized.
	 */
	void initialize();

	/**
	 * @param name Field name.
	 * @param desc Field desc.
	 * @return field offset for this object or {@code -1},
	 * if not found.
	 */
	long getFieldOffset(String name, String desc);
}
