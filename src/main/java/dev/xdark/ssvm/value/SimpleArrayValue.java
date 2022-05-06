package dev.xdark.ssvm.value;

import dev.xdark.ssvm.memory.Memory;

/**
 * Base implementation of array value.
 *
 * @author xDark
 */
public final class SimpleArrayValue extends SimpleObjectValue implements ArrayValue {

	/**
	 * @param memory
	 * 		Object data.
	 */
	public SimpleArrayValue(Memory memory) {
		super(memory);
	}

	@Override
	public long getLong(int index) {
		return getMemoryManager().readLong(this, dataOffset(validate(index) * sizeof(long.class)));
	}

	@Override
	public double getDouble(int index) {
		return getMemoryManager().readDouble(this, dataOffset(validate(index) * sizeof(double.class)));
	}

	@Override
	public int getInt(int index) {
		return getMemoryManager().readInt(this, dataOffset(validate(index) * sizeof(int.class)));
	}

	@Override
	public float getFloat(int index) {
		return getMemoryManager().readFloat(this, dataOffset(validate(index) * sizeof(float.class)));
	}

	@Override
	public char getChar(int index) {
		return getMemoryManager().readChar(this, dataOffset(validate(index) * sizeof(char.class)));
	}

	@Override
	public short getShort(int index) {
		return getMemoryManager().readShort(this, dataOffset(validate(index) * sizeof(short.class)));
	}

	@Override
	public byte getByte(int index) {
		return getMemoryManager().readByte(this, dataOffset(validate(index) * sizeof(byte.class)));
	}

	@Override
	public boolean getBoolean(int index) {
		return getMemoryManager().readBoolean(this, dataOffset(validate(index) * sizeof(boolean.class)));
	}

	@Override
	public Value getValue(int index) {
		return getMemoryManager().readValue(this, dataOffset(validate(index) * sizeof(Value.class)));
	}

	@Override
	public void setLong(int index, long value) {
		getMemoryManager().writeLong(this, dataOffset(validate(index) * sizeof(long.class)), value);
	}

	@Override
	public void setDouble(int index, double value) {
		getMemoryManager().writeDouble(this, dataOffset(validate(index) * sizeof(double.class)), value);
	}

	@Override
	public void setInt(int index, int value) {
		getMemoryManager().writeInt(this, dataOffset(validate(index) * sizeof(int.class)), value);
	}

	@Override
	public void setFloat(int index, float value) {
		getMemoryManager().writeFloat(this, dataOffset(validate(index) * sizeof(float.class)), value);
	}

	@Override
	public void setChar(int index, char value) {
		getMemoryManager().writeChar(this, dataOffset(validate(index) * sizeof(char.class)), value);
	}

	@Override
	public void setShort(int index, short value) {
		getMemoryManager().writeShort(this, dataOffset(validate(index) * sizeof(short.class)), value);
	}

	@Override
	public void setByte(int index, byte value) {
		getMemoryManager().writeByte(this, dataOffset(validate(index) * sizeof(byte.class)), value);
	}

	@Override
	public void setBoolean(int index, boolean value) {
		getMemoryManager().writeBoolean(this, dataOffset(validate(index) * sizeof(boolean.class)), value);
	}

	@Override
	public void setValue(int index, ObjectValue value) {
		getMemoryManager().writeValue(this, dataOffset(validate(index) * sizeof(Value.class)), value);
	}

	@Override
	public int getLength() {
		return getMemoryManager().readArrayLength(this);
	}

	private long sizeof(Class<?> c) {
		return getMemoryManager().arrayIndexScale(c);
	}

	private long dataOffset(long offset) {
		if (offset == -1L) {
			return offset;
		}
		return getMemoryManager().arrayBaseOffset(this) + offset;
	}

	private int validate(int index) {
		if (index < 0 || index >= getLength()) {
			throw new ArrayIndexOutOfBoundsException(Integer.toString(index));
		}
		return index;
	}
}
