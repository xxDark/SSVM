package dev.xdark.ssvm.value;

import dev.xdark.ssvm.memory.Memory;
import dev.xdark.ssvm.mirror.InstanceJavaClass;

/**
 * Base implementation of instance value.
 *
 * @author xDark
 */
public class SimpleInstanceValue extends SimpleObjectValue implements InstanceValue {

	private boolean initialized;

	/**
	 * @param memory
	 * 		Object data.
	 */
	public SimpleInstanceValue(Memory memory) {
		super(memory);
	}

	@Override
	public final InstanceJavaClass getJavaClass() {
		return (InstanceJavaClass) super.getJavaClass();
	}

	@Override
	public boolean isUninitialized() {
		return !initialized;
	}

	@Override
	public long getLong(String field) {
		return getMemoryManager().readLong(this, getFieldOffset(field, "J"));
	}

	@Override
	public double getDouble(String field) {
		return getMemoryManager().readDouble(this, getFieldOffset(field, "D"));
	}

	@Override
	public int getInt(String field) {
		return getMemoryManager().readInt(this, getFieldOffset(field, "I"));
	}

	@Override
	public float getFloat(String field) {
		return getMemoryManager().readFloat(this, getFieldOffset(field, "F"));
	}

	@Override
	public char getChar(String field) {
		return getMemoryManager().readChar(this, getFieldOffset(field, "C"));
	}

	@Override
	public short getShort(String field) {
		return getMemoryManager().readShort(this, getFieldOffset(field, "S"));
	}

	@Override
	public byte getByte(String field) {
		return getMemoryManager().readByte(this, getFieldOffset(field, "B"));
	}

	@Override
	public boolean getBoolean(String field) {
		return getMemoryManager().readBoolean(this, getFieldOffset(field, "Z"));
	}

	@Override
	public ObjectValue getValue(String field, String desc) {
		return getMemoryManager().readValue(this, getFieldOffset(field, desc));
	}

	@Override
	public void setLong(String field, long value) {
		getMemoryManager().writeLong(this, getFieldOffset(field, "J"), value);
	}

	@Override
	public void setDouble(String field, double value) {
		getMemoryManager().writeDouble(this, getFieldOffset(field, "D"), value);
	}

	@Override
	public void setInt(String field, int value) {
		getMemoryManager().writeInt(this, getFieldOffset(field, "I"), value);
	}

	@Override
	public void setFloat(String field, float value) {
		getMemoryManager().writeFloat(this, getFieldOffset(field, "F"), value);
	}

	@Override
	public void setChar(String field, char value) {
		getMemoryManager().writeChar(this, getFieldOffset(field, "C"), value);
	}

	@Override
	public void setShort(String field, short value) {
		getMemoryManager().writeShort(this, getFieldOffset(field, "S"), value);
	}

	@Override
	public void setByte(String field, byte value) {
		getMemoryManager().writeByte(this, getFieldOffset(field, "B"), value);
	}

	@Override
	public void setBoolean(String field, boolean value) {
		getMemoryManager().writeBoolean(this, getFieldOffset(field, "Z"), value);
	}

	@Override
	public void setValue(String field, String desc, ObjectValue value) {
		getMemoryManager().writeValue(this, getFieldOffset(field, desc), value);
	}

	@Override
	public void initialize() {
		initialized = true;
	}

	@Override
	public long getFieldOffset(String name, String desc) {
		long offset = getJavaClass().getVirtualFieldOffsetRecursively(name, desc);
		return offset == -1L ? -1L : getMemoryManager().valueBaseOffset(this) + offset;
	}

	@Override
	public String toString() {
		return getJavaClass().getInternalName();
	}
}
