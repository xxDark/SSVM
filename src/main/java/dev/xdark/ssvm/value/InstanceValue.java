package dev.xdark.ssvm.value;

import dev.xdark.ssvm.memory.Memory;
import dev.xdark.ssvm.mirror.InstanceJavaClass;

/**
 * Represents instance value.
 * (Arrays are represent differently).
 *
 * @author xDark
 */
public class InstanceValue extends ObjectValue {

	private boolean initialized;

	/**
	 * @param memory
	 * 		Object data.
	 */
	public InstanceValue(Memory memory) {
		super(memory);
	}

	@Override
	public InstanceJavaClass getJavaClass() {
		return (InstanceJavaClass) super.getJavaClass();
	}

	@Override
	public boolean isUninitialized() {
		return !initialized;
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
	public float getFloat(String field) {
		return getMemoryManager().readFloat(this, getFieldOffset(field, "F"));
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
	public ObjectValue getValue(String field, String desc) {
		return getMemoryManager().readValue(this, getFieldOffset(field, desc));
	}

	/**
	 * Sets long value of a field.
	 *
	 * @param field
	 * 		field name.
	 * @param value
	 * 		Value to set.
	 */
	public void setLong(String field, long value) {
		getMemoryManager().writeLong(this, getFieldOffset(field, "J"), value);
	}

	/**
	 * Sets double value of a field.
	 *
	 * @param field
	 * 		field name.
	 * @param value
	 * 		Value to set.
	 */
	public void setDouble(String field, double value) {
		getMemoryManager().writeDouble(this, getFieldOffset(field, "D"), value);
	}

	/**
	 * Sets int value of a field.
	 *
	 * @param field
	 * 		field name.
	 * @param value
	 * 		Value to set.
	 */
	public void setInt(String field, int value) {
		getMemoryManager().writeInt(this, getFieldOffset(field, "I"), value);
	}

	/**
	 * Sets float value of a field.
	 *
	 * @param field
	 * 		field name.
	 * @param value
	 * 		Value to set.
	 */
	public void setFloat(String field, float value) {
		getMemoryManager().writeFloat(this, getFieldOffset(field, "F"), value);
	}

	/**
	 * Sets char value of a field.
	 *
	 * @param field
	 * 		field name.
	 * @param value
	 * 		Value to set.
	 */
	public void setChar(String field, char value) {
		getMemoryManager().writeChar(this, getFieldOffset(field, "C"), value);
	}

	/**
	 * Sets short value of a field.
	 *
	 * @param field
	 * 		field name.
	 * @param value
	 * 		Value to set.
	 */
	public void setShort(String field, short value) {
		getMemoryManager().writeShort(this, getFieldOffset(field, "S"), value);
	}

	/**
	 * Sets byte value of a field.
	 *
	 * @param field
	 * 		field name.
	 * @param value
	 * 		Value to set.
	 */
	public void setByte(String field, byte value) {
		getMemoryManager().writeByte(this, getFieldOffset(field, "B"), value);
	}

	/**
	 * Sets boolean value of a field.
	 *
	 * @param field
	 * 		field name.
	 * @param value
	 * 		Value to set.
	 */
	public void setBoolean(String field, boolean value) {
		getMemoryManager().writeBoolean(this, getFieldOffset(field, "Z"), value);
	}

	/**
	 * Sets VM value of a field.
	 *
	 * @param field
	 * 		field name.
	 * @param desc
	 * 		Field descriptor.
	 * @param value
	 * 		Value to set.
	 */
	public void setValue(String field, String desc, ObjectValue value) {
		getMemoryManager().writeValue(this, getFieldOffset(field, desc), value);
	}

	/**
	 * Marks this object as initialized.
	 */
	public void initialize() {
		initialized = true;
	}

	/**
	 * @param name
	 * 		Field name.
	 * @param desc
	 * 		Field desc.
	 *
	 * @return field offset for this object or {@code -1},
	 * if not found.
	 */
	public long getFieldOffset(String name, String desc) {
		long offset = getJavaClass().getFieldOffsetRecursively(name, desc);
		return offset == -1L ? -1L : getMemoryManager().valueBaseOffset(this) + offset;
	}

	@Override
	public String toString() {
		return getJavaClass().getInternalName();
	}
}
