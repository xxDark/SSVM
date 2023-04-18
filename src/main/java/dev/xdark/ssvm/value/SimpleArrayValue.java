package dev.xdark.ssvm.value;

import dev.xdark.ssvm.memory.management.MemoryManager;
import dev.xdark.ssvm.memory.allocation.MemoryBlock;

/**
 * Base implementation of array value.
 *
 * @author xDark
 */
public final class SimpleArrayValue extends SimpleObjectValue implements ArrayValue {

	/**
	 * @param memoryManager Memory manager.
	 * @param memory        Object data.
	 */
	public SimpleArrayValue(MemoryManager memoryManager, MemoryBlock memory) {
		super(memoryManager, memory);
	}

	@Override
	public long getLong(int index) {
		MemoryManager memoryManager = getMemoryManager();
		return getData().readLong(dataOffset(validate(index) * (long) memoryManager.longSize()));
	}

	@Override
	public double getDouble(int index) {
		MemoryManager memoryManager = getMemoryManager();
		return Double.longBitsToDouble(getData().readLong(dataOffset(validate(index) * (long) memoryManager.doubleSize())));
	}

	@Override
	public int getInt(int index) {
		MemoryManager memoryManager = getMemoryManager();
		return getData().readInt(dataOffset(validate(index) * (long) memoryManager.intSize()));
	}

	@Override
	public float getFloat(int index) {
		MemoryManager memoryManager = getMemoryManager();
		return Float.intBitsToFloat(getData().readInt(dataOffset(validate(index) * (long) memoryManager.floatSize())));
	}

	@Override
	public char getChar(int index) {
		MemoryManager memoryManager = getMemoryManager();
		return getData().readChar(dataOffset(validate(index) * (long) memoryManager.charSize()));
	}

	@Override
	public short getShort(int index) {
		MemoryManager memoryManager = getMemoryManager();
		return getData().readShort(dataOffset(validate(index) * (long) memoryManager.shortSize()));
	}

	@Override
	public byte getByte(int index) {
		MemoryManager memoryManager = getMemoryManager();
		return getData().readByte(dataOffset(validate(index) * (long) memoryManager.byteSize()));
	}

	@Override
	public boolean getBoolean(int index) {
		MemoryManager memoryManager = getMemoryManager();
		return getData().readByte(dataOffset(validate(index) * (long) memoryManager.booleanSize())) != 0;
	}

	@Override
	public ObjectValue getReference(int index) {
		MemoryManager memoryManager = getMemoryManager();
		return memoryManager.readReference(this, dataOffset(validate(index) * (long) memoryManager.objectSize()));
	}

	@Override
	public void setLong(int index, long value) {
		MemoryManager memoryManager = getMemoryManager();
		getData().writeLong(dataOffset(validate(index) * (long) memoryManager.longSize()), value);
	}

	@Override
	public void setDouble(int index, double value) {
		MemoryManager memoryManager = getMemoryManager();
		getData().writeLong(dataOffset(validate(index) * (long) memoryManager.doubleSize()), Double.doubleToRawLongBits(value));
	}

	@Override
	public void setInt(int index, int value) {
		MemoryManager memoryManager = getMemoryManager();
		getData().writeInt(dataOffset(validate(index) * (long) memoryManager.intSize()), value);
	}

	@Override
	public void setFloat(int index, float value) {
		MemoryManager memoryManager = getMemoryManager();
		getData().writeInt(dataOffset(validate(index) * (long) memoryManager.floatSize()), Float.floatToRawIntBits(value));
	}

	@Override
	public void setChar(int index, char value) {
		MemoryManager memoryManager = getMemoryManager();
		getData().writeChar(dataOffset(validate(index) * (long) memoryManager.charSize()), value);
	}

	@Override
	public void setShort(int index, short value) {
		MemoryManager memoryManager = getMemoryManager();
		getData().writeShort(dataOffset(validate(index) * (long) memoryManager.shortSize()), value);
	}

	@Override
	public void setByte(int index, byte value) {
		MemoryManager memoryManager = getMemoryManager();
		getData().writeByte(dataOffset(validate(index) * (long) memoryManager.byteSize()), value);
	}

	@Override
	public void setBoolean(int index, boolean value) {
		MemoryManager memoryManager = getMemoryManager();
		getData().writeByte(dataOffset(validate(index) * (long) memoryManager.booleanSize()), (byte) (value ? 1 : 0));
	}

	@Override
	public void setReference(int index, ObjectValue value) {
		MemoryManager memoryManager = getMemoryManager();
		memoryManager.writeValue(this, dataOffset(validate(index) * (long) memoryManager.objectSize()), value);
	}

	@Override
	public int getLength() {
		return getMemoryManager().readArrayLength(this);
	}

	@Override
	public boolean isUninitialized() {
		return false;
	}

	@Override
	public boolean isNull() {
		return false;
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
