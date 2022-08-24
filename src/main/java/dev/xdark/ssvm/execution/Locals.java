package dev.xdark.ssvm.execution;

import dev.xdark.ssvm.value.ObjectValue;

/**
 * Storage for local variables.
 *
 * @author xDark
 */
public interface Locals {

	/**
	 * Sets value at specific index.
	 *
	 * @param index Index of local variable.
	 * @param value Value to set.
	 */
	void setReference(int index, ObjectValue value);

	/**
	 * Sets long value.
	 *
	 * @param index Variable index.
	 * @param value Value to set.
	 */
	void setLong(int index, long value);

	/**
	 * Sets double value.
	 *
	 * @param index Variable index.
	 * @param value Value to set.
	 */
	void setDouble(int index, double value);

	/**
	 * Sets int value.
	 *
	 * @param index Variable index.
	 * @param value Value to set.
	 */
	void setInt(int index, int value);

	/**
	 * Sets float value.
	 *
	 * @param index Variable index.
	 * @param value Value to set.
	 */
	void setFloat(int index, float value);

	/**
	 * Loads reference value from local variable.
	 *
	 * @param index Index of local variable.
	 * @return value at {@code index}.
	 */
	<V extends ObjectValue> V loadReference(int index);

	/**
	 * Loads long value.
	 *
	 * @param index Variable index.
	 * @return long value.
	 */
	long loadLong(int index);

	/**
	 * Loads double value.
	 *
	 * @param index Variable index.
	 * @return double value.
	 */
	double loadDouble(int index);

	/**
	 * Loads int value.
	 *
	 * @param index Variable index.
	 * @return int value.
	 */
	int loadInt(int index);

	/**
	 * Loads float value.
	 *
	 * @param index Variable index.
	 * @return float value.
	 */
	float loadFloat(int index);

	/**
	 * Copies contents into this locals.
	 *
	 * @param locals     Content to copy.
	 * @param srcOffset  Content offset.
	 * @param destOffset Offset to copy to.
	 * @param length     Content length.
	 */
	void copyFrom(Locals locals, int srcOffset, int destOffset, int length);

	/**
	 * @return the maximum amount of slots of this LVT.
	 */
	int maxSlots();
}
