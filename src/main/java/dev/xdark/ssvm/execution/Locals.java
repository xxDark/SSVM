package dev.xdark.ssvm.execution;

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
	 * Loads value from local variable.
	 *
	 * @param index Index of local variable.
	 * @param <V>   Type of the value to load.
	 * @return value at {@code index}.
	 */
	<V extends Value> V load(int index);

	/**
	 * Copies contents into this locals.
	 *
	 * @param locals Content to copy.
	 */
	void copyFrom(Value[] locals);

	/**
	 * @return underlying content of this LVT.
	 */
	Value[] getTable();

	/**
	 * @return the maximum amount of slots of this LVT.
	 */
	int maxSlots();
}
