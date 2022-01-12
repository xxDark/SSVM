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
	 * Reallocates {@literal direct} memory.
	 *
	 * @param address
	 * 		Address of the block.
	 * @param bytes
	 * 		How much memory to reallocate.
	 *
	 * @return Buffer representing allocated memory.
	 */
	Memory reallocateDirect(long address, long bytes);

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
	 * @return {@code true} if block was deallocated.
	 */
	boolean freeMemory(long address);

	/**
	 * Returns memory block based off it's address.
	 *
	 * @param address
	 * 		Memory address.
	 *
	 * @return memory block or {@code null},
	 * if not found.
	 */
	Memory getMemory(long address);

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
	 * Returns object at the specific address.
	 *
	 * @param address
	 * 		Address of the object.
	 *
	 * @return object or {@code null},
	 * if not found.
	 */
	Value getValue(long address);

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
	 * 		Type of Java value.
	 *
	 * @return allocated Java wrapper.
	 */
	<V> JavaValue<V> newJavaInstance(InstanceJavaClass javaClass, V value);

	/**
	 * Allocates new Java wrapper for java/lang/Class.
	 *
	 * @param javaClass
	 * 		java/lang/Class mirror.
	 * @param value
	 * 		Java value.
	 * @param <V>
	 * 		Type of Java value.
	 *
	 * @return allocated Java wrapper.
	 */
	<V> JavaValue<V> newJavaLangClass(InstanceJavaClass javaClass, V value);

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
	long readLong(ObjectValue object, long offset);

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
	double readDouble(ObjectValue object, long offset);

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
	int readInt(ObjectValue object, long offset);

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
	float readFloat(ObjectValue object, long offset);

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
	char readChar(ObjectValue object, long offset);

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
	short readShort(ObjectValue object, long offset);

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
	byte readByte(ObjectValue object, long offset);

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
	boolean readBoolean(ObjectValue object, long offset);

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
	Object readOop(ObjectValue object, long offset);

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
	ObjectValue readValue(ObjectValue object, long offset);

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
	void writeLong(ObjectValue object, long offset, long value);

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
	void writeDouble(ObjectValue object, long offset, double value);


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
	void writeInt(ObjectValue object, long offset, int value);

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
	void writeFloat(ObjectValue object, long offset, float value);

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
	void writeChar(ObjectValue object, long offset, char value);

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
	void writeShort(ObjectValue object, long offset, short value);

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
	void writeByte(ObjectValue object, long offset, byte value);

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
	void writeBoolean(ObjectValue object, long offset, boolean value);

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
	void writeOop(ObjectValue object, long offset, Object value);

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
	void writeValue(ObjectValue object, long offset, ObjectValue value);

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
	ObjectValue readValue(ArrayValue array, long offset);

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
	void writeValue(ArrayValue array, long offset, ObjectValue value);

	/**
	 * Creates and sets new class oop.
	 *
	 * @param javaClass
	 * 		Class to create oop for.
	 *
	 * @return oop.
	 */
	InstanceValue setOopForClass(JavaClass javaClass);

	/**
	 * Returns byte order of memory.
	 *
	 * @return byte order of memory.
	 */
	ByteOrder getByteOrder();

	/**
	 * Returns address size.
	 *
	 * @return address size.
	 */
	int addressSize();

	/**
	 * Returns page size.
	 *
	 * @return page size.
	 */
	int pageSize();

	/**
	 * Reports the offset of the data in
	 * the storage allocation of a given object.
	 *
	 * @param value
	 * 		Value to get offset from.
	 *
	 * @return offset of data.
	 */
	int valueBaseOffset(ObjectValue value);

	/**
	 * Reports the offset of the data in
	 * the storage allocation of a given class.
	 *
	 * @param value
	 * 		Class to get offset from.
	 *
	 * @return offset of data.
	 */
	int valueBaseOffset(JavaClass value);

	/**
	 * Reports the offset of the first element in
	 * the storage allocation of a given array class.
	 *
	 * @param javaClass
	 * 		Array component class.
	 *
	 * @return offset of the first element.
	 */
	int arrayBaseOffset(JavaClass javaClass);

	/**
	 * Reports the offset of the first element in
	 * the storage allocation of a given array class.
	 *
	 * @param javaClass
	 * 		Array component class.
	 *
	 * @return offset of the first element.
	 */
	int arrayBaseOffset(Class<?> javaClass);

	/**
	 * Reports the index scale for elements in
	 * the storage allocation of a given array class.
	 *
	 * @param javaClass
	 * 		Array component class.
	 *
	 * @return index scale for elements in the array.
	 */
	int arrayIndexScale(JavaClass javaClass);

	/**
	 * Reports the index scale for elements in
	 * the storage allocation of a given array class.
	 *
	 * @param javaClass
	 * 		Array component class.
	 *
	 * @return index scale for elements in the array.
	 */
	int arrayIndexScale(Class<?> javaClass);

	/**
	 * Returns memory block at location {@code 0}.
	 *
	 * @return memory block at location {@code 0}.
	 */
	Memory zero();

	/**
	 * Returns static data offset for the class.
	 *
	 * @param jc
	 * 		Class to get static data offset for.
	 *
	 * @return static data offset.
	 */
	long getStaticOffset(JavaClass jc);
}
