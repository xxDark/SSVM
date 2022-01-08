package dev.xdark.ssvm.memory;

import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.mirror.JavaClass;
import dev.xdark.ssvm.value.ObjectValue;
import dev.xdark.ssvm.value.Value;

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
	ObjectValue newObject(InstanceJavaClass javaClass);

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
	Value readValue(ObjectValue object, long offset);

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
	 * Creates new class oop.
	 *
	 * @param javaClass
	 * 		Class to create oop for.
	 *
	 * @return oop.
	 */
	Value newOopForClass(JavaClass javaClass);
}
