package dev.xdark.ssvm.memory.management;

import dev.xdark.ssvm.mirror.type.ArrayClass;
import dev.xdark.ssvm.mirror.type.InstanceClass;
import dev.xdark.ssvm.mirror.type.JavaClass;
import dev.xdark.ssvm.synchronizer.Mutex;
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
public interface MemoryManager extends ReferenceMap {

	/**
	 * @return {@code null} value.
	 */
	ObjectValue nullValue();

	/**
	 * @param reference Reference to get synchronizer for.
	 * @return Object synchronizer.
	 */
	Mutex getMutex(ObjectValue reference);

	/**
	 * Allocates new object.
	 *
	 * @param javaClass Class of the object.
	 * @return allocated object.
	 */
	InstanceValue tryNewInstance(InstanceClass javaClass);

	/**
	 * Allocates new object.
	 * Throws VM exception if allocation fails.
	 *
	 * @param javaClass Class of the object.
	 * @return allocated object.
	 */
	InstanceValue newInstance(InstanceClass javaClass);

	/**
	 * Allocates new Java wrapper.
	 *
	 * @param javaClass Class of the object.
	 * @param value     Java value.
	 * @param <V>       Type of Java value.
	 * @return allocated Java wrapper.
	 */
	<V> JavaValue<V> tryNewJavaInstance(InstanceClass javaClass, V value);

	/**
	 * Allocates new Java wrapper.
	 * Throws VM exception if allocation fails.
	 *
	 * @param javaClass Class of the object.
	 * @param value     Java value.
	 * @param <V>       Type of Java value.
	 * @return allocated Java wrapper.
	 */
	<V> JavaValue<V> newJavaInstance(InstanceClass javaClass, V value);

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
	JavaValue<InstanceClass> newJavaLangClass(InstanceClass javaClass);

	/**
	 * Allocates new array.
	 * Throws VM exception if allocation fails.
	 *
	 * @param javaClass Array class.
	 * @param length    Array length.
	 * @return allocated array.
	 */
	ArrayValue newArray(ArrayClass javaClass, int length);

	/**
	 * Reads VM value from an object.
	 *
	 * @param object Object to read value from.
	 * @param offset Field offset.
	 * @return read VM value.
	 * @throws IllegalStateException if {@code offset} is negative.
	 */
	ObjectValue readReference(ObjectValue object, long offset);

	/**
	 * Reads object class form an object.
	 *
	 * @param object Object to read class from.
	 * @return read class value.
	 * @throws IllegalStateException if {@code offset} is negative.
	 */
	JavaClass readClass(ObjectValue object);

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
	 * @return Oop.
	 */
	InstanceValue newClassOop(JavaClass javaClass);

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
	long sizeOfType(JavaClass javaClass);

	/**
	 * Reports the size of the type.
	 *
	 * @param javaClass Array component class.
	 * @return size of the type.
	 */
	long sizeOfType(Class<?> javaClass);

	/**
	 * @return Size of {@link Object} type.
	 */
	int objectSize();

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
}
