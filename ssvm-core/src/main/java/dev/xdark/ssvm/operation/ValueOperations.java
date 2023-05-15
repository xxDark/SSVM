package dev.xdark.ssvm.operation;

import dev.xdark.ssvm.value.ObjectValue;

/**
 * VM value operations.
 *
 * @author Matt Coley
 */
public interface ValueOperations {
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
