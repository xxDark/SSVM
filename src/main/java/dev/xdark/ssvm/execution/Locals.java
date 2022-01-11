package dev.xdark.ssvm.execution;

import dev.xdark.ssvm.value.Value;

import java.util.Objects;

/**
 * Storage for local variables.
 *
 * @author xDark
 */
public final class Locals {

	private final Value[] table;

	/**
	 * @param maxSize
	 * 		The maximum amount of local variables.
	 */
	public Locals(int maxSize) {
		table = new Value[maxSize];
	}

	/**
	 * Sets value at specific index.
	 *
	 * @param index
	 * 		Index of local variable.
	 * @param value
	 * 		Value to set.
	 */
	public void set(int index, Value value) {
		table[index] = Objects.requireNonNull(value, "value");
	}

	/**
	 * Loads value from local variable.
	 *
	 * @param index
	 * 		Index of local variable.
	 * @param <V>
	 * 		Type of the value to load.
	 *
	 * @return value at {@code index}.
	 */
	public <V extends Value> V load(int index) {
		return (V) table[index];
	}
}
