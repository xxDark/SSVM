package dev.xdark.ssvm.execution;

import dev.xdark.ssvm.value.DoubleValue;
import dev.xdark.ssvm.value.FloatValue;
import dev.xdark.ssvm.value.IntValue;
import dev.xdark.ssvm.value.LongValue;
import dev.xdark.ssvm.value.ObjectValue;
import dev.xdark.ssvm.value.Value;

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
	void set(int index, Value value);

	/**
	 * Sets wide value at specific index.
	 *
	 * @param index Index of local variable.
	 * @param value Value to set.
	 */
	void setWide(int index, Value value);

	/**
	 * Sets long value.
	 *
	 * @param index Variable index.
	 * @param value Value to set.
	 */
	default void setLong(int index, long value) {
		setWide(index, LongValue.of(value));
	}

	/**
	 * Sets double value.
	 *
	 * @param index Variable index.
	 * @param value Value to set.
	 */
	default void setDouble(int index, double value) {
		setWide(index, new DoubleValue(value));
	}

	/**
	 * Sets int value.
	 *
	 * @param index Variable index.
	 * @param value Value to set.
	 */
	default void setInt(int index, int value) {
		set(index, IntValue.of(value));
	}

	/**
	 * Sets float value.
	 *
	 * @param index Variable index.
	 * @param value Value to set.
	 */
	default void setFloat(int index, float value) {
		setWide(index, new FloatValue(value));
	}

	/**
	 * Loads value from local variable.
	 *
	 * @param index Index of local variable.
	 * @param <V>   Type of the value to load.
	 * @return value at {@code index}.
	 */
	<V extends Value> V load(int index);

	/**
	 * Loads reference value from local variable.
	 *
	 * @param index Index of local variable.
	 * @return value at {@code index}.
	 */
	default <V extends ObjectValue> V loadReference(int index) {
		return load(index);
	}

	/**
	 * Loads long value.
	 *
	 * @param index Variable index.
	 * @return long value.
	 */
	default long loadLong(int index) {
		return load(index).asLong();
	}

	/**
	 * Loads double value.
	 *
	 * @param index Variable index.
	 * @return double value.
	 */
	default double loadDouble(int index) {
		return load(index).asDouble();
	}

	/**
	 * Loads int value.
	 *
	 * @param index Variable index.
	 * @return int value.
	 */
	default int loadInt(int index) {
		return load(index).asInt();
	}

	/**
	 * Loads float value.
	 *
	 * @param index Variable index.
	 * @return float value.
	 */
	default float loadFloat(int index) {
		return load(index).asFloat();
	}

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
	 * Copies contents into this locals.
	 *
	 * @param locals     Content to copy.
	 * @param srcOffset  Content srcOffset.
	 * @param destOffset Offset to copy to.
	 * @param length     Content length.
	 */
	void copyFrom(Value[] locals, int srcOffset, int destOffset, int length);

	/**
	 * @return underlying content of this LVT.
	 */
	Value[] getTable();

	/**
	 * @return the maximum amount of slots of this LVT.
	 */
	int maxSlots();
}
