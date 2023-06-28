package dev.xdark.ssvm.operation;

import dev.xdark.ssvm.value.ObjectValue;

/**
 * VM operations for {@code java.lang.Object}.
 *
 * @author Matt Coley
 */
public interface ObjectOperations {
	/**
	 * @param value1 First value to compare.
	 * @param value2 Second value to compare.
	 * @return {@code true} when equal.
	 */
	boolean equals(ObjectValue value1, ObjectValue value2);

	/**
	 * @param value Value to call {@link Object#getClass()} on.
	 * @return The class of the value.
	 */
	ObjectValue getClass(ObjectValue value);

	/**
	 * @param value Value to call {@link Object#hashCode()} on.
	 * @return The hash code of the value.
	 */
	int hashCode(ObjectValue value);

	/**
	 * @param value Value to call {@link Object#toString()} on.
	 * @return The to-string representation.
	 */
	String toString(ObjectValue value);
}
