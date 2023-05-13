package dev.xdark.ssvm.operation;

import dev.xdark.ssvm.value.ArrayValue;
import dev.xdark.ssvm.value.InstanceValue;
import dev.xdark.ssvm.value.ObjectValue;

/**
 * VM string operations.
 *
 * @author xDark
 */
public interface StringOperations {

	/**
	 * @param value String to convert to VM utf-8 oop.
	 * @return String value.
	 */
	InstanceValue newUtf8(String value);

	/**
	 * @param value Byte array to convert to VM utf-8 oop.
	 * @return String value.
	 */
	InstanceValue newUtf8FromBytes(ArrayValue value);

	/**
	 * @param value Character array to convert to VM utf-8 oop.
	 * @return String value.
	 */
	InstanceValue newUtf8FromChars(ArrayValue value);

	/**
	 * @param value Value to read from.
	 * @return String value.
	 */
	String readUtf8(ObjectValue value);

	/**
	 * @param value String value.
	 * @return String value as an array of VM chars.
	 */
	ArrayValue toChars(String value);

	/**
	 * @param value String value.
	 * @return String value as an array of VM bytes.
	 */
	ArrayValue toBytes(String value);
}
