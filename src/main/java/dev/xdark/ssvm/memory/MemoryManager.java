package dev.xdark.ssvm.memory;

import dev.xdark.ssvm.mirror.ArrayJavaClass;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.mirror.JavaClass;
import dev.xdark.ssvm.value.*;

import java.nio.ByteOrder;

/**
 * VM memory manager.
 * Memory manager takes responsibility for
 * setting object field values, and managing Unsafe.
 *
 * @author xDark
 */
public interface MemoryManager {

	/**
	 * Allocates {@literal direct} memory.
	 *
	 * @param bytes
	 * 		How much memory to allocate.
	 *
	 * @return Buffer representing allocated memory.
	 */
	Memory allocateDirect(long bytes);

	/**
	 * Allocates {@literal heap} memory.
	 *
	 * @param bytes
	 * 		How much memory to allocate.
	 *
	 * @return Buffer representing allocated memory.
	 */
	Memory allocateHeap(long bytes);

	/**
	 * Deallocates block of direct memory.
	 *
	 * @param address
	 * 		Address of the memory block.
	 *
	 * @return {@code true} if memory was deallocated,
	 * {@code false} if address is invalid.
	 */
	boolean freeMemory(long address);

	/**
	 * Returns true if the address is valid.
	 * This method is usually used by Unsafe,
	 * if method returns {@code false}, VM will panic.
	 *
	 * @param address
	 * 		Address to check.
	 *
	 * @return {@code true} if address is valid,
	 * {@code false} otherwise.
	 */
	boolean isValidAddress(long address);

	/**
	 * Allocates new object.
	 *
	 * @param javaClass
	 * 		Class of the object.
	 *
	 * @return allocated object.
	 */
	InstanceValue newInstance(InstanceJavaClass javaClass);

	/**
	 * Allocates new Java wrapper.
	 *
	 * @param javaClass
	 * 		Class of the object.
	 * @param value
	 * 		Java value.
	 * @param <V>
	 * 		Type of Jav value.
	 *
	 * @return allocated Java wrapper.
	 */
	<V> JavaValue<V> newJavaInstance(InstanceJavaClass javaClass, V value);

	/**
	 * Allocates new array.
	 *
	 * @param javaClass
	 * 		Array class.
	 * @param length
	 * 		Array length.
	 * @param componentSize
	 * 		Size of each component.
	 *
	 * @return allocated array.
	 */
	ArrayValue newArray(ArrayJavaClass javaClass, int length, long componentSize);

	/**
	 * Reads long from an object.
	 *
	 * @param object
	 * 		Object to read long from.
	 * @param offset
	 * 		Field offset.
	 *
	 * @return read long value.
	 *
	 * @throws IllegalStateException
	 * 		if {@code offset} is negative.
	 */
	long readLong(InstanceValue object, long offset);

	/**
	 * Reads double from an object.
	 *
	 * @param object
	 * 		Object to read double from.
	 * @param offset
	 * 		Field offset.
	 *
	 * @return read double value.
	 *
	 * @throws IllegalStateException
	 * 		if {@code offset} is negative.
	 */
	double readDouble(InstanceValue object, long offset);

	/**
	 * Reads int from an object.
	 *
	 * @param object
	 * 		Object to read int from.
	 * @param offset
	 * 		Field offset.
	 *
	 * @return read int value.
	 *
	 * @throws IllegalStateException
	 * 		if {@code offset} is negative.
	 */
	int readInt(InstanceValue object, long offset);

	/**
	 * Reads float from an object.
	 *
	 * @param object
	 * 		Object to read float from.
	 * @param offset
	 * 		Field offset.
	 *
	 * @return read float value.
	 *
	 * @throws IllegalStateException
	 * 		if {@code offset} is negative.
	 */
	float readFloat(InstanceValue object, long offset);

	/**
	 * Reads char from an object.
	 *
	 * @param object
	 * 		Object to read char from.
	 * @param offset
	 * 		Field offset.
	 *
	 * @return read char value.
	 *
	 * @throws IllegalStateException
	 * 		if {@code offset} is negative.
	 */
	char readChar(InstanceValue object, long offset);

	/**
	 * Reads short from an object.
	 *
	 * @param object
	 * 		Object to read short from.
	 * @param offset
	 * 		Field offset.
	 *
	 * @return read short value.
	 *
	 * @throws IllegalStateException
	 * 		if {@code offset} is negative.
	 */
	short readShort(InstanceValue object, long offset);

	/**
	 * Reads byte from an object.
	 *
	 * @param object
	 * 		Object to read byte from.
	 * @param offset
	 * 		Field offset.
	 *
	 * @return read byte value.
	 *
	 * @throws IllegalStateException
	 * 		if {@code offset} is negative.
	 */
	byte readByte(InstanceValue object, long offset);

	/**
	 * Reads boolean from an object.
	 *
	 * @param object
	 * 		Object to read boolean from.
	 * @param offset
	 * 		Field offset.
	 *
	 * @return read boolean value.
	 *
	 * @throws IllegalStateException
	 * 		if {@code offset} is negative.
	 */
	boolean readBoolean(InstanceValue object, long offset);

	/**
	 * Reads an object from an object.
	 *
	 * @param object
	 * 		Object to read object from.
	 * @param offset
	 * 		Field offset.
	 *
	 * @return read object value.
	 *
	 * @throws IllegalStateException
	 * 		if {@code offset} is negative.
	 */
	Object readOop(InstanceValue object, long offset);

	/**
	 * Reads VM value from an object.
	 *
	 * @param object
	 * 		Object to read value from.
	 * @param offset
	 * 		Field offset.
	 *
	 * @return read VM value.
	 *
	 * @throws IllegalStateException
	 * 		if {@code offset} is negative.
	 */
	Value readValue(InstanceValue object, long offset);

	/**
	 * Reads object class form an object.
	 *
	 * @param object
	 * 		Object to read class from.
	 *
	 * @return read class value.
	 *
	 * @throws IllegalStateException
	 * 		if {@code offset} is negative.
	 */
	JavaClass readClass(ObjectValue object);

	/**
	 * Writes long to the object.
	 *
	 * @param object
	 * 		Object to write to.
	 * @param offset
	 * 		Field offset.
	 * @param value
	 * 		Value to write.
	 *
	 * @throws IllegalStateException
	 * 		if {@code offset} is negative.
	 */
	void writeLong(InstanceValue object, long offset, long value);

	/**
	 * Writes double to the object.
	 *
	 * @param object
	 * 		Object to write to.
	 * @param offset
	 * 		Field offset.
	 * @param value
	 * 		Value to write.
	 *
	 * @throws IllegalStateException
	 * 		if {@code offset} is negative.
	 */
	void writeDouble(InstanceValue object, long offset, double value);


	/**
	 * Writes int to the object.
	 *
	 * @param object
	 * 		Object to write to.
	 * @param offset
	 * 		Field offset.
	 * @param value
	 * 		Value to write.
	 *
	 * @throws IllegalStateException
	 * 		if {@code offset} is negative.
	 */
	void writeInt(InstanceValue object, long offset, int value);

	/**
	 * Writes float to the object.
	 *
	 * @param object
	 * 		Object to write to.
	 * @param offset
	 * 		Field offset.
	 * @param value
	 * 		Value to write.
	 *
	 * @throws IllegalStateException
	 * 		if {@code offset} is negative.
	 */
	void writeFloat(InstanceValue object, long offset, float value);

	/**
	 * Writes char to the object.
	 *
	 * @param object
	 * 		Object to write to.
	 * @param offset
	 * 		Field offset.
	 * @param value
	 * 		Value to write.
	 *
	 * @throws IllegalStateException
	 * 		if {@code offset} is negative.
	 */
	void writeChar(InstanceValue object, long offset, char value);

	/**
	 * Writes short to the object.
	 *
	 * @param object
	 * 		Object to write to.
	 * @param offset
	 * 		Field offset.
	 * @param value
	 * 		Value to write.
	 *
	 * @throws IllegalStateException
	 * 		if {@code offset} is negative.
	 */
	void writeShort(InstanceValue object, long offset, short value);

	/**
	 * Writes byte to the object.
	 *
	 * @param object
	 * 		Object to write to.
	 * @param offset
	 * 		Field offset.
	 * @param value
	 * 		Value to write.
	 *
	 * @throws IllegalStateException
	 * 		if {@code offset} is negative.
	 */
	void writeByte(InstanceValue object, long offset, byte value);

	/**
	 * Writes boolean to the object.
	 *
	 * @param object
	 * 		Object to write to.
	 * @param offset
	 * 		Field offset.
	 * @param value
	 * 		Value to write.
	 *
	 * @throws IllegalStateException
	 * 		if {@code offset} is negative.
	 */
	void writeBoolean(InstanceValue object, long offset, boolean value);

	/**
	 * Writes oop into the object.
	 *
	 * @param object
	 * 		Object to write to.
	 * @param offset
	 * 		Field offset.
	 *
	 * @throws IllegalStateException
	 * 		if {@code offset} is negative.
	 */
	void writeOop(InstanceValue object, long offset, Object value);

	/**
	 * Writes VM value to the object.
	 *
	 * @param object
	 * 		Object to write to.
	 * @param offset
	 * 		Field offset.
	 * @param value
	 * 		Value to write.
	 *
	 * @throws IllegalStateException
	 * 		if {@code offset} is negative.
	 */
	void writeValue(InstanceValue object, long offset, Value value);


	/**
	 * Reads long from an array.
	 *
	 * @param array
	 * 		Array to read long from.
	 * @param offset
	 * 		Field offset.
	 *
	 * @return read long value.
	 *
	 * @throws IllegalStateException
	 * 		if {@code offset} is negative.
	 */
	long readLong(ArrayValue array, long offset);

	/**
	 * Reads double from an array.
	 *
	 * @param array
	 * 		Array to read double from.
	 * @param offset
	 * 		Field offset.
	 *
	 * @return read double value.
	 *
	 * @throws IllegalStateException
	 * 		if {@code offset} is negative.
	 */
	double readDouble(ArrayValue array, long offset);

	/**
	 * Reads int from an array.
	 *
	 * @param array
	 * 		Array to read int from.
	 * @param offset
	 * 		Field offset.
	 *
	 * @return read int value.
	 *
	 * @throws IllegalStateException
	 * 		if {@code offset} is negative.
	 */
	int readInt(ArrayValue array, long offset);

	/**
	 * Reads float from an array.
	 *
	 * @param array
	 * 		Array to read float from.
	 * @param offset
	 * 		Field offset.
	 *
	 * @return read float value.
	 *
	 * @throws IllegalStateException
	 * 		if {@code offset} is negative.
	 */
	float readFloat(ArrayValue array, long offset);

	/**
	 * Reads char from an array.
	 *
	 * @param array
	 * 		Array to read char from.
	 * @param offset
	 * 		Field offset.
	 *
	 * @return read char value.
	 *
	 * @throws IllegalStateException
	 * 		if {@code offset} is negative.
	 */
	char readChar(ArrayValue array, long offset);

	/**
	 * Reads short from an array.
	 *
	 * @param array
	 * 		Array to read short from.
	 * @param offset
	 * 		Field offset.
	 *
	 * @return read short value.
	 *
	 * @throws IllegalStateException
	 * 		if {@code offset} is negative.
	 */
	short readShort(ArrayValue array, long offset);

	/**
	 * Reads byte from an array.
	 *
	 * @param array
	 * 		Array to read byte from.
	 * @param offset
	 * 		Field offset.
	 *
	 * @return read byte value.
	 *
	 * @throws IllegalStateException
	 * 		if {@code offset} is negative.
	 */
	byte readByte(ArrayValue array, long offset);

	/**
	 * Reads boolean from an array.
	 *
	 * @param array
	 * 		Array to read boolean from.
	 * @param offset
	 * 		Field offset.
	 *
	 * @return read boolean value.
	 *
	 * @throws IllegalStateException
	 * 		if {@code offset} is negative.
	 */
	boolean readBoolean(ArrayValue array, long offset);

	/**
	 * Reads an array from an array.
	 *
	 * @param array
	 * 		Array to read array from.
	 * @param offset
	 * 		Field offset.
	 *
	 * @return read array value.
	 *
	 * @throws IllegalStateException
	 * 		if {@code offset} is negative.
	 */
	Object readOop(ArrayValue array, long offset);

	/**
	 * Reads VM value from an array.
	 *
	 * @param array
	 * 		Array to read value from.
	 * @param offset
	 * 		Field offset.
	 *
	 * @return read VM value.
	 *
	 * @throws IllegalStateException
	 * 		if {@code offset} is negative.
	 */
	Value readValue(ArrayValue array, long offset);

	/**
	 * Reads array length.
	 *
	 * @param array
	 * 		Array to get length from.
	 *
	 * @return length of the array.
	 */
	int readArrayLength(ArrayValue array);

	/**
	 * Writes long to the array.
	 *
	 * @param array
	 * 		Array to write to.
	 * @param offset
	 * 		Field offset.
	 * @param value
	 * 		Value to write.
	 *
	 * @throws IllegalStateException
	 * 		if {@code offset} is negative.
	 */
	void writeLong(ArrayValue array, long offset, long value);

	/**
	 * Writes double to the array.
	 *
	 * @param array
	 * 		Array to write to.
	 * @param offset
	 * 		Field offset.
	 * @param value
	 * 		Value to write.
	 *
	 * @throws IllegalStateException
	 * 		if {@code offset} is negative.
	 */
	void writeDouble(ArrayValue array, long offset, double value);


	/**
	 * Writes int to the array.
	 *
	 * @param array
	 * 		Array to write to.
	 * @param offset
	 * 		Field offset.
	 * @param value
	 * 		Value to write.
	 *
	 * @throws IllegalStateException
	 * 		if {@code offset} is negative.
	 */
	void writeInt(ArrayValue array, long offset, int value);

	/**
	 * Writes float to the array.
	 *
	 * @param array
	 * 		Array to write to.
	 * @param offset
	 * 		Field offset.
	 * @param value
	 * 		Value to write.
	 *
	 * @throws IllegalStateException
	 * 		if {@code offset} is negative.
	 */
	void writeFloat(ArrayValue array, long offset, float value);

	/**
	 * Writes char to the array.
	 *
	 * @param array
	 * 		Array to write to.
	 * @param offset
	 * 		Field offset.
	 * @param value
	 * 		Value to write.
	 *
	 * @throws IllegalStateException
	 * 		if {@code offset} is negative.
	 */
	void writeChar(ArrayValue array, long offset, char value);

	/**
	 * Writes short to the array.
	 *
	 * @param array
	 * 		Array to write to.
	 * @param offset
	 * 		Field offset.
	 * @param value
	 * 		Value to write.
	 *
	 * @throws IllegalStateException
	 * 		if {@code offset} is negative.
	 */
	void writeShort(ArrayValue array, long offset, short value);

	/**
	 * Writes byte to the array.
	 *
	 * @param array
	 * 		Array to write to.
	 * @param offset
	 * 		Field offset.
	 * @param value
	 * 		Value to write.
	 *
	 * @throws IllegalStateException
	 * 		if {@code offset} is negative.
	 */
	void writeByte(ArrayValue array, long offset, byte value);

	/**
	 * Writes boolean to the array.
	 *
	 * @param array
	 * 		Array to write to.
	 * @param offset
	 * 		Field offset.
	 * @param value
	 * 		Value to write.
	 *
	 * @throws IllegalStateException
	 * 		if {@code offset} is negative.
	 */
	void writeBoolean(ArrayValue array, long offset, boolean value);

	/**
	 * Writes oop into the array.
	 *
	 * @param array
	 * 		Array to write to.
	 * @param offset
	 * 		Field offset.
	 *
	 * @throws IllegalStateException
	 * 		if {@code offset} is negative.
	 */
	void writeOop(ArrayValue array, long offset, Object value);

	/**
	 * Writes VM value to the array.
	 *
	 * @param array
	 * 		Array to write to.
	 * @param offset
	 * 		Field offset.
	 * @param value
	 * 		Value to write.
	 *
	 * @throws IllegalStateException
	 * 		if {@code offset} is negative.
	 */
	void writeValue(ArrayValue array, long offset, Value value);

	/**
	 * Creates new class oop.
	 *
	 * @param javaClass
	 * 		Class to create oop for.
	 *
	 * @return oop.
	 */
	Value newOopForClass(JavaClass javaClass);

	/**
	 * Returns byte order of memory.
	 *
	 * @return byte order of memory.
	 */
	ByteOrder getByteOrder();
}
