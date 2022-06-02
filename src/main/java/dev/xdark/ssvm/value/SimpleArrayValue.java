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
		return getMemoryManager().readLong(this, dataOffset(validate(index) * (long) getMemoryManager().longSize()));
	}

	@Override
	public double getDouble(int index) {
		return getMemoryManager().readDouble(this, dataOffset(validate(index) * (long) getMemoryManager().doubleSize()));
	}

	@Override
	public int getInt(int index) {
		return getMemoryManager().readInt(this, dataOffset(validate(index) * (long) getMemoryManager().intSize()));
	}

	@Override
	public float getFloat(int index) {
		return getMemoryManager().readFloat(this, dataOffset(validate(index) * (long) getMemoryManager().floatSize()));
	}

	@Override
	public char getChar(int index) {
		return getMemoryManager().readChar(this, dataOffset(validate(index) * (long) getMemoryManager().charSize()));
	}

	@Override
	public short getShort(int index) {
		return getMemoryManager().readShort(this, dataOffset(validate(index) * (long) getMemoryManager().shortSize()));
	}

	@Override
	public byte getByte(int index) {
		return getMemoryManager().readByte(this, dataOffset(validate(index) * (long) getMemoryManager().byteSize()));
	}

	@Override
	public boolean getBoolean(int index) {
		return getMemoryManager().readBoolean(this, dataOffset(validate(index) * (long) getMemoryManager().booleanSize()));
	}

	@Override
	public Value getValue(int index) {
		return getMemoryManager().readValue(this, dataOffset(validate(index) * (long) getMemoryManager().objectSize()));
	}

	@Override
	public void setLong(int index, long value) {
		getMemoryManager().writeLong(this, dataOffset(validate(index) * (long) getMemoryManager().longSize()), value);
	}

	@Override
	public void setDouble(int index, double value) {
		getMemoryManager().writeDouble(this, dataOffset(validate(index) * (long) getMemoryManager().doubleSize()), value);
	}

	@Override
	public void setInt(int index, int value) {
		getMemoryManager().writeInt(this, dataOffset(validate(index) * (long) getMemoryManager().intSize()), value);
	}

	@Override
	public void setFloat(int index, float value) {
		getMemoryManager().writeFloat(this, dataOffset(validate(index) * (long) getMemoryManager().floatSize()), value);
	}

	@Override
	public void setChar(int index, char value) {
		getMemoryManager().writeChar(this, dataOffset(validate(index) * (long) getMemoryManager().charSize()), value);
	}

	@Override
	public void setShort(int index, short value) {
		getMemoryManager().writeShort(this, dataOffset(validate(index) * (long) getMemoryManager().shortSize()), value);
	}

	@Override
	public void setByte(int index, byte value) {
		getMemoryManager().writeByte(this, dataOffset(validate(index) * (long) getMemoryManager().byteSize()), value);
	}

	@Override
	public void setBoolean(int index, boolean value) {
		getMemoryManager().writeBoolean(this, dataOffset(validate(index) * (long) getMemoryManager().booleanSize()), value);
	}

	@Override
	public void setValue(int index, ObjectValue value) {
		getMemoryManager().writeValue(this, dataOffset(validate(index) * (long) getMemoryManager().objectSize()), value);
	}

	@Override
	public int getLength() {
		return getMemoryManager().readArrayLength(this);
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
