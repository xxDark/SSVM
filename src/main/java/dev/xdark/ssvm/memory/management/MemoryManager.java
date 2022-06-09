package dev.xdark.ssvm.memory.management;

import dev.xdark.ssvm.memory.gc.GarbageCollector;
import dev.xdark.ssvm.mirror.ArrayJavaClass;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.mirror.JavaClass;
import dev.xdark.ssvm.value.ArrayValue;
import dev.xdark.ssvm.value.InstanceValue;
import dev.xdark.ssvm.value.JavaValue;
import dev.xdark.ssvm.value.ObjectValue;

import java.util.Collection;

/**
 * VM memory manager.
 * Memory manager takes responsibility for
 * setting object field values, and managing Unsafe.
 *
 * @author xDark
 */
public interface MemoryManager {

	/**
	 * @return {@code null} value.
	 */
	ObjectValue nullValue();

	/**
	 * Returns object at the specific address.
	 *
	 * @param address Address of the object.
	 * @return object or {@code null},
	 * if not found.
	 */
	ObjectValue getValue(long address);

	/**
	 * Allocates new object.
	 *
	 * @param javaClass Class of the object.
	 * @return allocated object.
	 */
	InstanceValue tryNewInstance(InstanceJavaClass javaClass);

	/**
	 * Allocates new object.
	 * Throws VM exception if allocation fails.
	 *
	 * @param javaClass Class of the object.
	 * @return allocated object.
	 */
	InstanceValue newInstance(InstanceJavaClass javaClass);

	/**
	 * Allocates new Java wrapper.
	 *
	 * @param javaClass Class of the object.
	 * @param value     Java value.
	 * @param <V>       Type of Java value.
	 * @return allocated Java wrapper.
	 */
	<V> JavaValue<V> tryNewJavaInstance(InstanceJavaClass javaClass, V value);

	/**
	 * Allocates new Java wrapper.
	 * Throws VM exception if allocation fails.
	 *
	 * @param javaClass Class of the object.
	 * @param value     Java value.
	 * @param <V>       Type of Java value.
	 * @return allocated Java wrapper.
	 */
	<V> JavaValue<V> newJavaInstance(InstanceJavaClass javaClass, V value);

	/**
	 * Allocates new Java wrapper for {@code java/lang/Class}.
	 * The method must set class oop for {@code java/lang/Class} here,
	 * as it may happen that the memory manager might require virtual
	 * field layout for {@code java/lang/Class} to be initialized,
	 * in case if its not, it may fail.
	 *
	 * @param javaClass java/lang/Class mirror.
	 * @return allocated Java wrapper.
	 */
	JavaValue<InstanceJavaClass> newJavaLangClass(InstanceJavaClass javaClass);

	/**
	 * Allocates new array.
	 *
	 * @param javaClass Array class.
	 * @param length    Array length.
	 * @return allocated array.
	 */
	ArrayValue tryNewArray(ArrayJavaClass javaClass, int length);

	/**
	 * Allocates new array.
	 * Throws VM exception if allocation fails.
	 *
	 * @param javaClass Array class.
	 * @param length    Array length.
	 * @return allocated array.
	 */
	ArrayValue newArray(ArrayJavaClass javaClass, int length);

	/**
	 * Reads long from an object.
	 *
	 * @param object Object to read long from.
	 * @param offset Field offset.
	 * @return read long value.
	 * @throws IllegalStateException if {@code offset} is negative.
	 */
	long readLong(ObjectValue object, long offset);

	/**
	 * Reads double from an object.
	 *
	 * @param object Object to read double from.
	 * @param offset Field offset.
	 * @return read double value.
	 * @throws IllegalStateException if {@code offset} is negative.
	 */
	double readDouble(ObjectValue object, long offset);

	/**
	 * Reads int from an object.
	 *
	 * @param object Object to read int from.
	 * @param offset Field offset.
	 * @return read int value.
	 * @throws IllegalStateException if {@code offset} is negative.
	 */
	int readInt(ObjectValue object, long offset);

	/**
	 * Reads float from an object.
	 *
	 * @param object Object to read float from.
	 * @param offset Field offset.
	 * @return read float value.
	 * @throws IllegalStateException if {@code offset} is negative.
	 */
	float readFloat(ObjectValue object, long offset);

	/**
	 * Reads char from an object.
	 *
	 * @param object Object to read char from.
	 * @param offset Field offset.
	 * @return read char value.
	 * @throws IllegalStateException if {@code offset} is negative.
	 */
	char readChar(ObjectValue object, long offset);

	/**
	 * Reads short from an object.
	 *
	 * @param object Object to read short from.
	 * @param offset Field offset.
	 * @return read short value.
	 * @throws IllegalStateException if {@code offset} is negative.
	 */
	short readShort(ObjectValue object, long offset);

	/**
	 * Reads byte from an object.
	 *
	 * @param object Object to read byte from.
	 * @param offset Field offset.
	 * @return read byte value.
	 * @throws IllegalStateException if {@code offset} is negative.
	 */
	byte readByte(ObjectValue object, long offset);

	/**
	 * Reads boolean from an object.
	 *
	 * @param object Object to read boolean from.
	 * @param offset Field offset.
	 * @return read boolean value.
	 * @throws IllegalStateException if {@code offset} is negative.
	 */
	boolean readBoolean(ObjectValue object, long offset);

	/**
	 * Reads VM value from an object.
	 *
	 * @param object Object to read value from.
	 * @param offset Field offset.
	 * @return read VM value.
	 * @throws IllegalStateException if {@code offset} is negative.
	 */
	ObjectValue readValue(ObjectValue object, long offset);

	/**
	 * Reads object class form an object.
	 *
	 * @param object Object to read class from.
	 * @return read class value.
	 * @throws IllegalStateException if {@code offset} is negative.
	 */
	JavaClass readClass(ObjectValue object);

	/**
	 * Writes long to the object.
	 *
	 * @param object Object to write to.
	 * @param offset Field offset.
	 * @param value  Value to write.
	 * @throws IllegalStateException if {@code offset} is negative.
	 */
	void writeLong(ObjectValue object, long offset, long value);

	/**
	 * Writes double to the object.
	 *
	 * @param object Object to write to.
	 * @param offset Field offset.
	 * @param value  Value to write.
	 * @throws IllegalStateException if {@code offset} is negative.
	 */
	void writeDouble(ObjectValue object, long offset, double value);


	/**
	 * Writes int to the object.
	 *
	 * @param object Object to write to.
	 * @param offset Field offset.
	 * @param value  Value to write.
	 * @throws IllegalStateException if {@code offset} is negative.
	 */
	void writeInt(ObjectValue object, long offset, int value);

	/**
	 * Writes float to the object.
	 *
	 * @param object Object to write to.
	 * @param offset Field offset.
	 * @param value  Value to write.
	 * @throws IllegalStateException if {@code offset} is negative.
	 */
	void writeFloat(ObjectValue object, long offset, float value);

	/**
	 * Writes char to the object.
	 *
	 * @param object Object to write to.
	 * @param offset Field offset.
	 * @param value  Value to write.
	 * @throws IllegalStateException if {@code offset} is negative.
	 */
	void writeChar(ObjectValue object, long offset, char value);

	/**
	 * Writes short to the object.
	 *
	 * @param object Object to write to.
	 * @param offset Field offset.
	 * @param value  Value to write.
	 * @throws IllegalStateException if {@code offset} is negative.
	 */
	void writeShort(ObjectValue object, long offset, short value);

	/**
	 * Writes byte to the object.
	 *
	 * @param object Object to write to.
	 * @param offset Field offset.
	 * @param value  Value to write.
	 * @throws IllegalStateException if {@code offset} is negative.
	 */
	void writeByte(ObjectValue object, long offset, byte value);

	/**
	 * Writes boolean to the object.
	 *
	 * @param object Object to write to.
	 * @param offset Field offset.
	 * @param value  Value to write.
	 * @throws IllegalStateException if {@code offset} is negative.
	 */
	void writeBoolean(ObjectValue object, long offset, boolean value);

	/**
	 * Writes VM value to the object.
	 *
	 * @param object Object to write to.
	 * @param offset Field offset.
	 * @param value  Value to write.
	 * @throws IllegalStateException if {@code offset} is negative.
	 */
	void writeValue(ObjectValue object, long offset, ObjectValue value);

	/**
	 * Writes VM value to the object.
	 *
	 * @param object Object to write to.
	 * @param offset Field offset.
	 * @param value  Value to write.
	 * @throws IllegalStateException if {@code offset} is negative.
	 */
	ObjectValue getAndWriteValue(ObjectValue object, long offset, ObjectValue value);

	/**
	 * Reads array length.
	 *
	 * @param array Array to get length from.
	 * @return length of the array.
	 */
	int readArrayLength(ArrayValue array);

	/**
	 * Creates new class oop.
	 *
	 * @param javaClass Class to create oop for.
	 * @return oop.
	 */
	<C extends JavaClass> JavaValue<C> tryNewClassOop(C javaClass);

	/**
	 * Creates new class oop.
	 * Throws VM exception if allocation fails.
	 *
	 * @param javaClass Class to create oop for.
	 * @return oop.
	 */
	<C extends JavaClass> JavaValue<C> newClassOop(C javaClass);

	/**
	 * Reports the offset of the data in
	 * the storage allocation of a given object.
	 *
	 * @param value Value to get offset from.
	 * @return offset of data.
	 */
	int valueBaseOffset(ObjectValue value);

	/**
	 * Reports the offset of the data in
	 * the storage allocation of a given class.
	 *
	 * @param value Class to get offset from.
	 * @return offset of data.
	 */
	int valueBaseOffset(JavaClass value);

	/**
	 * Reports the offset of the first element in
	 * the storage allocation of a given array class.
	 *
	 * @param javaClass Array component class.
	 * @return offset of the first element.
	 */
	int arrayBaseOffset(JavaClass javaClass);

	/**
	 * Reports the offset of the first element in
	 * the storage allocation of a given array.
	 *
	 * @param array Array value.
	 * @return offset of the first element.
	 */
	int arrayBaseOffset(ArrayValue array);

	/**
	 * Reports the offset of the first element in
	 * the storage allocation of a given array class.
	 *
	 * @param javaClass Array component class.
	 * @return offset of the first element.
	 */
	int arrayBaseOffset(Class<?> javaClass);

	/**
	 * Reports the size of the type.
	 *
	 * @param javaClass Array component class.
	 * @return size of the type.
	 */
	int sizeOfType(JavaClass javaClass);

	/**
	 * Reports the size of the type.
	 *
	 * @param javaClass Array component class.
	 * @return size of the type.
	 */
	int sizeOfType(Class<?> javaClass);

	/**
	 * @return Size of {@code long} type.
	 */
	int longSize();

	/**
	 * @return Size of {@code double} type.
	 */
	int doubleSize();

	/**
	 * @return Size of {@code int} type.
	 */
	int intSize();

	/**
	 * @return Size of {@code float} type.
	 */
	int floatSize();

	/**
	 * @return Size of {@code char} type.
	 */
	int charSize();

	/**
	 * @return Size of {@code short} type.
	 */
	int shortSize();

	/**
	 * @return Size of {@code byte} type.
	 */
	int byteSize();

	/**
	 * @return Size of {@code boolean} type.
	 */
	int booleanSize();

	/**
	 * @return Size of {@link Object} type.
	 */
	int objectSize();

	/**
	 * Returns static data offset for the class.
	 *
	 * @param jc Class to get static data offset for.
	 * @return static data offset.
	 */
	long getStaticOffset(JavaClass jc);

	/**
	 * @return the collection of all allocated objects.
	 */
	Collection<ObjectValue> listObjects();

	/**
	 * Writes default memory data into
	 * the object.
	 *
	 * @param value Object to write data into.
	 */
	void writeDefaults(ObjectValue value);

	/**
	 * @return garbage collector.
	 */
	GarbageCollector getGarbageCollector();
}
