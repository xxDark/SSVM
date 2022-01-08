package dev.xdark.ssvm.value;

import dev.xdark.ssvm.memory.Memory;
import dev.xdark.ssvm.memory.MemoryManager;
import dev.xdark.ssvm.mirror.FieldInfo;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.mirror.JavaClass;

/**
 * Represents VM object value.
 *
 * @author xDark
 */
public class ObjectValue implements Value {

	private final Memory memory;

	/**
	 * @param memory
	 * 		Object data.
	 */
	public ObjectValue(Memory memory) {
		this.memory = memory;
	}

	@Override
	public <T> T as(Class<T> type) {
		throw new IllegalStateException(type.toString());
	}

	@Override
	public boolean isWide() {
		return false;
	}

	@Override
	public boolean isUninitialized() {
		return false;
	}

	@Override
	public boolean isNull() {
		return false;
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
	 * Returns object class.
	 *
	 * @return object class.
	 */
	public JavaClass getJavaClass() {
		return getMemoryManager().readClass(this);
	}

	/**
	 * Returns object data.
	 *
	 * @return object data.
	 */
	public Memory getMemory() {
		return memory;
	}

	private MemoryManager getMemoryManager() {
		return memory.getMemoryManager();
	}

	private long getFieldOffset(String name, String desc) {
		return getJavaClass().getLayout().getFieldOffset(new FieldInfo(name, desc));
	}
}
