package dev.xdark.ssvm.value;

import dev.xdark.ssvm.memory.Memory;

public final class ArrayValue extends ObjectValue {

	/**
	 * @param memory
	 * 		Object data.
	 */
	public ArrayValue(Memory memory) {
		super(memory);
	}

	/**
	 * Returns long value at specific index.
	 *
	 * @param index
	 * 		Index to get long from.
	 *
	 * @return long value.
	 */
	public long getLong(int index) {
		return getMemoryManager().readLong(this, dataOffset(validate(index) * sizeof(long.class)));
	}

	/**
	 * Returns double value at specific index.
	 *
	 * @param index
	 * 		Index to get double from.
	 *
	 * @return double value.
	 */
	public double getDouble(int index) {
		return getMemoryManager().readDouble(this, dataOffset(validate(index) * sizeof(double.class)));
	}

	/**
	 * Returns int value at specific index.
	 *
	 * @param index
	 * 		Index to get int from.
	 *
	 * @return double value.
	 */
	public int getInt(int index) {
		return getMemoryManager().readInt(this, dataOffset(validate(index) * sizeof(int.class)));
	}

	/**
	 * Returns float value at specific index.
	 *
	 * @param index
	 * 		Index to get float from.
	 *
	 * @return float value.
	 */
	public float getFloat(int index) {
		return getMemoryManager().readFloat(this, dataOffset(validate(index) * sizeof(float.class)));
	}

	/**
	 * Returns char value at specific index.
	 *
	 * @param index
	 * 		Index to get char from.
	 *
	 * @return char value.
	 */
	public char getChar(int index) {
		return getMemoryManager().readChar(this, dataOffset(validate(index) * sizeof(char.class)));
	}

	/**
	 * Returns short value at specific index.
	 *
	 * @param index
	 * 		Index to get short from.
	 *
	 * @return short value.
	 */
	public short getShort(int index) {
		return getMemoryManager().readShort(this, dataOffset(validate(index) * sizeof(short.class)));
	}

	/**
	 * Returns byte value at specific index.
	 *
	 * @param index
	 * 		Index to get byte from.
	 *
	 * @return byte value.
	 */
	public byte getByte(int index) {
		return getMemoryManager().readByte(this, dataOffset(validate(index) * sizeof(byte.class)));
	}

	/**
	 * Returns boolean value at specific index.
	 *
	 * @param index
	 * 		Index to get boolean from.
	 *
	 * @return boolean value.
	 */
	public boolean getBoolean(int index) {
		return getMemoryManager().readBoolean(this, dataOffset(validate(index) * sizeof(boolean.class)));
	}

	/**
	 * Returns VM value at specific index.
	 *
	 * @param index
	 * 		Index to get value from.
	 *
	 * @return VM value.
	 */
	public Value getValue(int index) {
		return getMemoryManager().readValue(this, dataOffset(validate(index) * sizeof(Value.class)));
	}

	/**
	 * Sets long value of an array.
	 *
	 * @param index
	 * 		Index to store value to.
	 * @param value
	 * 		Value to set.
	 */
	public void setLong(int index, long value) {
		getMemoryManager().writeLong(this, dataOffset(validate(index) * sizeof(long.class)), value);
	}

	/**
	 * Sets double value of an array.
	 *
	 * @param index
	 * 		Index to store value to.
	 * @param value
	 * 		Value to set.
	 */
	public void setDouble(int index, double value) {
		getMemoryManager().writeDouble(this, dataOffset(validate(index) * sizeof(double.class)), value);
	}

	/**
	 * Sets int value of an array.
	 *
	 * @param index
	 * 		Index to store value to.
	 * @param value
	 * 		Value to set.
	 */
	public void setInt(int index, int value) {
		getMemoryManager().writeInt(this, dataOffset(validate(index) * sizeof(int.class)), value);
	}

	/**
	 * Sets float value of an array.
	 *
	 * @param index
	 * 		Index to store value to.
	 * @param value
	 * 		Value to set.
	 */
	public void setFloat(int index, float value) {
		getMemoryManager().writeFloat(this, dataOffset(validate(index) * sizeof(float.class)), value);
	}

	/**
	 * Sets char value of an array.
	 *
	 * @param index
	 * 		Index to store value to.
	 * @param value
	 * 		Value to set.
	 */
	public void setChar(int index, char value) {
		getMemoryManager().writeChar(this, dataOffset(validate(index) * sizeof(char.class)), value);
	}

	/**
	 * Sets short value of an array.
	 *
	 * @param index
	 * 		Index to store value to.
	 * @param value
	 * 		Value to set.
	 */
	public void setShort(int index, short value) {
		getMemoryManager().writeShort(this, dataOffset(validate(index) * sizeof(short.class)), value);
	}

	/**
	 * Sets byte value of an array.
	 *
	 * @param index
	 * 		Index to store value to.
	 * @param value
	 * 		Value to set.
	 */
	public void setByte(int index, byte value) {
		getMemoryManager().writeByte(this, dataOffset(validate(index) * sizeof(byte.class)), value);
	}

	/**
	 * Sets boolean value of an array.
	 *
	 * @param index
	 * 		Index to store value to.
	 * @param value
	 * 		Value to set.
	 */
	public void setBoolean(int index, boolean value) {
		getMemoryManager().writeBoolean(this, dataOffset(validate(index) * sizeof(boolean.class)), value);
	}

	/**
	 * Sets VM value of an array.
	 *
	 * @param index
	 * 		Index to store value to.
	 * @param value
	 * 		Value to set.
	 */
	public void setValue(int index, Value value) {
		getMemoryManager().writeValue(this, dataOffset(validate(index) * sizeof(Value.class)), value);
	}

	/**
	 * Returns array length.
	 *
	 * @return array length.
	 */
	public int getLength() {
		return getMemoryManager().readArrayLength(this);
	}

	private long sizeof(Class<?> c) {
		return getMemoryManager().arrayIndexScale(c);
	}

	private long dataOffset(long offset) {
		if (offset == -1L) return offset;
		return getMemoryManager().arrayBaseOffset(getJavaClass()) + offset;
	}

	private int validate(int index) {
		if (index < 0 || index >= getLength()) {
			throw new ArrayIndexOutOfBoundsException(Integer.toString(index));
		}
		return index;
	}
}
