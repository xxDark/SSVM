package dev.xdark.ssvm.value;

import dev.xdark.ssvm.LanguageSpecification;
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
		return getData().readLong(dataOffset(validate(index) * LanguageSpecification.LONG_SIZE));
	}

	@Override
	public double getDouble(int index) {
		return Double.longBitsToDouble(getData().readLong(dataOffset(validate(index) * LanguageSpecification.DOUBLE_SIZE)));
	}

	@Override
	public int getInt(int index) {
		return getData().readInt(dataOffset(validate(index) * LanguageSpecification.INT_SIZE));
	}

	@Override
	public float getFloat(int index) {
		return Float.intBitsToFloat(getData().readInt(dataOffset(validate(index) * LanguageSpecification.FLOAT_SIZE)));
	}

	@Override
	public char getChar(int index) {
		return getData().readChar(dataOffset(validate(index) * LanguageSpecification.CHAR_SIZE));
	}

	@Override
	public short getShort(int index) {
		return getData().readShort(dataOffset(validate(index) * LanguageSpecification.SHORT_SIZE));
	}

	@Override
	public byte getByte(int index) {
		return getData().readByte(dataOffset(validate(index) * LanguageSpecification.BYTE_SIZE));
	}

	@Override
	public boolean getBoolean(int index) {
		return getData().readByte(dataOffset(validate(index) * LanguageSpecification.BOOLEAN_SIZE)) != 0;
	}

	@Override
	public ObjectValue getReference(int index) {
		MemoryManager memoryManager = getMemoryManager();
		return memoryManager.readReference(this, dataOffset(validate(index) * (long) memoryManager.objectSize()));
	}

	@Override
	public void setLong(int index, long value) {
		getData().writeLong(dataOffset(validate(index) * LanguageSpecification.LONG_SIZE), value);
	}

	@Override
	public void setDouble(int index, double value) {
		getData().writeLong(dataOffset(validate(index) * LanguageSpecification.DOUBLE_SIZE), Double.doubleToRawLongBits(value));
	}

	@Override
	public void setInt(int index, int value) {
		getData().writeInt(dataOffset(validate(index) * LanguageSpecification.INT_SIZE), value);
	}

	@Override
	public void setFloat(int index, float value) {
		getData().writeInt(dataOffset(validate(index) * LanguageSpecification.FLOAT_SIZE), Float.floatToRawIntBits(value));
	}

	@Override
	public void setChar(int index, char value) {
		getData().writeChar(dataOffset(validate(index) * LanguageSpecification.CHAR_SIZE), value);
	}

	@Override
	public void setShort(int index, short value) {
		getData().writeShort(dataOffset(validate(index) * LanguageSpecification.SHORT_SIZE), value);
	}

	@Override
	public void setByte(int index, byte value) {
		getData().writeByte(dataOffset(validate(index) * LanguageSpecification.BYTE_SIZE), value);
	}

	@Override
	public void setBoolean(int index, boolean value) {
		getData().writeByte(dataOffset(validate(index) * LanguageSpecification.BOOLEAN_SIZE), (byte) (value ? 1 : 0));
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
