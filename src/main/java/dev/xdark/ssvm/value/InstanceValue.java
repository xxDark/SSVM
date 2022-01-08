package dev.xdark.ssvm.value;

import dev.xdark.ssvm.memory.Memory;
import dev.xdark.ssvm.mirror.FieldInfo;

/**
 * Represents instance value.
 * (Arrays are represent differently).
 *
 * @author xDark
 */
public class InstanceValue extends ObjectValue {

	/**
	 * @param memory
	 * 		Object data.
	 */
	public InstanceValue(Memory memory) {
		super(memory);
	}

	/**
	 * Returns long value of a field.
	 *
	 * @param field
	 * 		Field name.
	 *
	 * @return long value.
	 */
	public long getLong(String field) {
		return getMemoryManager().readLong(this, getFieldOffset(field, "J"));
	}

	/**
	 * Returns double value of a field.
	 *
	 * @param field
	 * 		Field name.
	 *
	 * @return double value.
	 */
	public double getDouble(String field) {
		return getMemoryManager().readDouble(this, getFieldOffset(field, "D"));
	}

	/**
	 * Returns int value of a field.
	 *
	 * @param field
	 * 		Field name.
	 *
	 * @return int value.
	 */
	public int getInt(String field) {
		return getMemoryManager().readInt(this, getFieldOffset(field, "I"));
	}

	/**
	 * Returns float value of a field.
	 *
	 * @param field
	 * 		Field name.
	 *
	 * @return float value.
	 */
	public int getFloat(String field) {
		return getMemoryManager().readInt(this, getFieldOffset(field, "F"));
	}

	/**
	 * Returns char value of a field.
	 *
	 * @param field
	 * 		Field name.
	 *
	 * @return char value.
	 */
	public char getChar(String field) {
		return getMemoryManager().readChar(this, getFieldOffset(field, "C"));
	}

	/**
	 * Returns short value of a field.
	 *
	 * @param field
	 * 		Field name.
	 *
	 * @return short value.
	 */
	public short getShort(String field) {
		return getMemoryManager().readShort(this, getFieldOffset(field, "S"));
	}

	/**
	 * Returns byte value of a field.
	 *
	 * @param field
	 * 		Field name.
	 *
	 * @return byte value.
	 */
	public byte getByte(String field) {
		return getMemoryManager().readByte(this, getFieldOffset(field, "B"));
	}

	/**
	 * Returns boolean value of a field.
	 *
	 * @param field
	 * 		Field name.
	 *
	 * @return boolean value.
	 */
	public boolean getBoolean(String field) {
		return getMemoryManager().readBoolean(this, getFieldOffset(field, "Z"));
	}

	/**
	 * Returns VM value of a field.
	 *
	 * @param field
	 * 		Field name.
	 *
	 * @return VM value.
	 */
	public Value getValue(String field, String desc) {
		return getMemoryManager().readValue(this, getFieldOffset(field, desc));
	}

	private long getFieldOffset(String name, String desc) {
		return getJavaClass().getLayout().getFieldOffset(new FieldInfo(name, desc));
	}
}
