package dev.xdark.ssvm.operation;

import dev.xdark.ssvm.value.ObjectValue;

/**
 * VM array operations.
 *
 * @author xDark
 */
public interface ArrayOperations {

	/**
	 * Throws VM exception if array is null.
	 *
	 * @param value Array to get length for.
	 * @return length of the array.
	 */
	int getArrayLength(ObjectValue value);

	/**
	 * Loads value from the array.
	 * Throws VM exception if array is null
	 * or index is out of bounds.
	 *
	 * @param array Array to get value from.
	 * @param index Value index.
	 * @return value from the array.
	 */
	ObjectValue arrayLoadReference(ObjectValue array, int index);

	/**
	 * Loads value from the array.
	 * Throws VM exception if array is null
	 * or index is out of bounds.
	 *
	 * @param array Array to get value from.
	 * @param index Value index.
	 * @return value from the array.
	 */
	long arrayLoadLong(ObjectValue array, int index);

	/**
	 * Loads value from the array.
	 * Throws VM exception if array is null
	 * or index is out of bounds.
	 *
	 * @param array Array to get value from.
	 * @param index Value index.
	 * @return value from the array.
	 */
	double arrayLoadDouble(ObjectValue array, int index);

	/**
	 * Loads value from the array.
	 * Throws VM exception if array is null
	 * or index is out of bounds.
	 *
	 * @param array Array to get value from.
	 * @param index Value index.
	 * @return value from the array.
	 */
	int arrayLoadInt(ObjectValue array, int index);

	/**
	 * Loads value from the array.
	 * Throws VM exception if array is null
	 * or index is out of bounds.
	 *
	 * @param array Array to get value from.
	 * @param index Value index.
	 * @return value from the array.
	 */
	float arrayLoadFloat(ObjectValue array, int index);

	/**
	 * Loads value from the array.
	 * Throws VM exception if array is null
	 * or index is out of bounds.
	 *
	 * @param array Array to get value from.
	 * @param index Value index.
	 * @return value from the array.
	 */
	char arrayLoadChar(ObjectValue array, int index);

	/**
	 * Loads value from the array.
	 * Throws VM exception if array is null
	 * or index is out of bounds.
	 *
	 * @param array Array to get value from.
	 * @param index Value index.
	 * @return value from the array.
	 */
	short arrayLoadShort(ObjectValue array, int index);

	/**
	 * Loads value from the array.
	 * Throws VM exception if array is null
	 * or index is out of bounds.
	 *
	 * @param array Array to get value from.
	 * @param index Value index.
	 * @return value from the array.
	 */
	byte arrayLoadByte(ObjectValue array, int index);

	/**
	 * Loads value from the array.
	 * Throws VM exception if array is null
	 * or index is out of bounds.
	 *
	 * @param array Array to get value from.
	 * @param index Value index.
	 * @return value from the array.
	 */
	boolean arrayLoadBoolean(ObjectValue array, int index);

	/**
	 * Sets value in the array.
	 * Throws VM exception if array is null
	 * or index is out of bounds.
	 *
	 * @param array Array to set value in.
	 * @param index Value index.
	 * @param value Value to set.
	 */
	void arrayStoreReference(ObjectValue array, int index, ObjectValue value);

	/**
	 * Sets value in the array.
	 * Throws VM exception if array is null
	 * or index is out of bounds.
	 *
	 * @param array Array to set value in.
	 * @param index Value index.
	 * @param value Value to set.
	 */
	void arrayStoreLong(ObjectValue array, int index, long value);

	/**
	 * Sets value in the array.
	 * Throws VM exception if array is null
	 * or index is out of bounds.
	 *
	 * @param array Array to set value in.
	 * @param index Value index.
	 * @param value Value to set.
	 */
	void arrayStoreDouble(ObjectValue array, int index, double value);

	/**
	 * Sets value in the array.
	 * Throws VM exception if array is null
	 * or index is out of bounds.
	 *
	 * @param array Array to set value in.
	 * @param index Value index.
	 * @param value Value to set.
	 */
	void arrayStoreInt(ObjectValue array, int index, int value);

	/**
	 * Sets value in the array.
	 * Throws VM exception if array is null
	 * or index is out of bounds.
	 *
	 * @param array Array to set value in.
	 * @param index Value index.
	 * @param value Value to set.
	 */
	void arrayStoreFloat(ObjectValue array, int index, float value);

	/**
	 * Sets value in the array.
	 * Throws VM exception if array is null
	 * or index is out of bounds.
	 *
	 * @param array Array to set value in.
	 * @param index Value index.
	 * @param value Value to set.
	 */
	void arrayStoreChar(ObjectValue array, int index, char value);

	/**
	 * Sets value in the array.
	 * Throws VM exception if array is null
	 * or index is out of bounds.
	 *
	 * @param array Array to set value in.
	 * @param index Value index.
	 * @param value Value to set.
	 */
	void arrayStoreShort(ObjectValue array, int index, short value);

	/**
	 * Sets value in the array.
	 * Throws VM exception if array is null
	 * or index is out of bounds.
	 *
	 * @param array Array to set value in.
	 * @param index Value index.
	 * @param value Value to set.
	 */
	void arrayStoreByte(ObjectValue array, int index, byte value);
}
