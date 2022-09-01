package dev.xdark.ssvm.util;

import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.mirror.JavaClass;
import dev.xdark.ssvm.value.ArrayValue;
import dev.xdark.ssvm.value.InstanceValue;
import dev.xdark.ssvm.value.ObjectValue;

/**
 * VM operations.
 *
 * @author xDark
 */
public interface VMOperations {

	/**
	 * Attempts to allocate new instance of a type of a class.
	 * Throws VM exception if allocation fails.
	 *
	 * @param klass Class to allocate an instance of.
	 * @return allocated instance.
	 * @see InstanceJavaClass#canAllocateInstance()
	 */
	InstanceValue allocateInstance(JavaClass klass);

	/**
	 * Allocates new array with a specific length.
	 * Throws VM exception if length is invalid.
	 *
	 * @param componentType Array component type.
	 * @param length        Array length.
	 * @return new array.
	 */
	ArrayValue allocateArray(JavaClass componentType, int length);

	/**
	 * Allocates new long array with a specific length.
	 * Throws VM exception if length is invalid.
	 *
	 * @param length Array length.
	 * @return new array.
	 */
	ArrayValue allocateLongArray(int length);

	/**
	 * Allocates new double array with a specific length.
	 * Throws VM exception if length is invalid.
	 *
	 * @param length Array length.
	 * @return new array.
	 */
	ArrayValue allocateDoubleArray(int length);

	/**
	 * Allocates new int array with a specific length.
	 * Throws VM exception if length is invalid.
	 *
	 * @param length Array length.
	 * @return new array.
	 */
	ArrayValue allocateIntArray(int length);

	/**
	 * Allocates new float array with a specific length.
	 * Throws VM exception if length is invalid.
	 *
	 * @param length Array length.
	 * @return new array.
	 */
	ArrayValue allocateFloatArray(int length);

	/**
	 * Allocates new char array with a specific length.
	 * Throws VM exception if length is invalid.
	 *
	 * @param length Array length.
	 * @return new array.
	 */
	ArrayValue allocateCharArray(int length);

	/**
	 * Allocates new short array with a specific length.
	 * Throws VM exception if length is invalid.
	 *
	 * @param length Array length.
	 * @return new array.
	 */
	ArrayValue allocateShortArray(int length);

	/**
	 * Allocates new byte array with a specific length.
	 * Throws VM exception if length is invalid.
	 *
	 * @param length Array length.
	 * @return new array.
	 */
	ArrayValue allocateByteArray(int length);

	/**
	 * Allocates new boolean array with a specific length.
	 * Throws VM exception if length is invalid.
	 *
	 * @param length Array length.
	 * @return new array.
	 */
	ArrayValue allocateBooleanArray(int length);

	/**
	 * Throws VM exception if array is null.
	 *
	 * @param value Array to get length for.
	 * @return length of the array.
	 */
	int getArrayLength(ObjectValue value);

	/**
	 * Loads value from the array.
	 * Throws VM exception if array is null
	 * or index is out of bounds.
	 *
	 * @param array Array to get value from.
	 * @param index Value index.
	 * @return value from the array.
	 */
	ObjectValue arrayLoadReference(ObjectValue array, int index);

	/**
	 * Loads value from the array.
	 * Throws VM exception if array is null
	 * or index is out of bounds.
	 *
	 * @param array Array to get value from.
	 * @param index Value index.
	 * @return value from the array.
	 */
	long arrayLoadLong(ObjectValue array, int index);

	/**
	 * Loads value from the array.
	 * Throws VM exception if array is null
	 * or index is out of bounds.
	 *
	 * @param array Array to get value from.
	 * @param index Value index.
	 * @return value from the array.
	 */
	double arrayLoadDouble(ObjectValue array, int index);

	/**
	 * Loads value from the array.
	 * Throws VM exception if array is null
	 * or index is out of bounds.
	 *
	 * @param array Array to get value from.
	 * @param index Value index.
	 * @return value from the array.
	 */
	int arrayLoadInt(ObjectValue array, int index);

	/**
	 * Loads value from the array.
	 * Throws VM exception if array is null
	 * or index is out of bounds.
	 *
	 * @param array Array to get value from.
	 * @param index Value index.
	 * @return value from the array.
	 */
	float arrayLoadFloat(ObjectValue array, int index);

	/**
	 * Loads value from the array.
	 * Throws VM exception if array is null
	 * or index is out of bounds.
	 *
	 * @param array Array to get value from.
	 * @param index Value index.
	 * @return value from the array.
	 */
	char arrayLoadChar(ObjectValue array, int index);

	/**
	 * Loads value from the array.
	 * Throws VM exception if array is null
	 * or index is out of bounds.
	 *
	 * @param array Array to get value from.
	 * @param index Value index.
	 * @return value from the array.
	 */
	short arrayLoadShort(ObjectValue array, int index);

	/**
	 * Loads value from the array.
	 * Throws VM exception if array is null
	 * or index is out of bounds.
	 *
	 * @param array Array to get value from.
	 * @param index Value index.
	 * @return value from the array.
	 */
	byte arrayLoadByte(ObjectValue array, int index);

	/**
	 * Loads value from the array.
	 * Throws VM exception if array is null
	 * or index is out of bounds.
	 *
	 * @param array Array to get value from.
	 * @param index Value index.
	 * @return value from the array.
	 */
	boolean arrayLoadBoolean(ObjectValue array, int index);

	/**
	 * Sets value in the array.
	 * Throws VM exception if array is null
	 * or index is out of bounds.
	 *
	 * @param array Array to set value in.
	 * @param index Value index.
	 * @param value Value to set.
	 */
	void arrayStoreReference(ObjectValue array, int index, ObjectValue value);

	/**
	 * Sets value in the array.
	 * Throws VM exception if array is null
	 * or index is out of bounds.
	 *
	 * @param array Array to set value in.
	 * @param index Value index.
	 * @param value Value to set.
	 */
	void arrayStoreLong(ObjectValue array, int index, long value);

	/**
	 * Sets value in the array.
	 * Throws VM exception if array is null
	 * or index is out of bounds.
	 *
	 * @param array Array to set value in.
	 * @param index Value index.
	 * @param value Value to set.
	 */
	void arrayStoreDouble(ObjectValue array, int index, double value);

	/**
	 * Sets value in the array.
	 * Throws VM exception if array is null
	 * or index is out of bounds.
	 *
	 * @param array Array to set value in.
	 * @param index Value index.
	 * @param value Value to set.
	 */
	void arrayStoreInt(ObjectValue array, int index, int value);

	/**
	 * Sets value in the array.
	 * Throws VM exception if array is null
	 * or index is out of bounds.
	 *
	 * @param array Array to set value in.
	 * @param index Value index.
	 * @param value Value to set.
	 */
	void arrayStoreFloat(ObjectValue array, int index, float value);

	/**
	 * Sets value in the array.
	 * Throws VM exception if array is null
	 * or index is out of bounds.
	 *
	 * @param array Array to set value in.
	 * @param index Value index.
	 * @param value Value to set.
	 */
	void arrayStoreChar(ObjectValue array, int index, char value);

	/**
	 * Sets value in the array.
	 * Throws VM exception if array is null
	 * or index is out of bounds.
	 *
	 * @param array Array to set value in.
	 * @param index Value index.
	 * @param value Value to set.
	 */
	void arrayStoreShort(ObjectValue array, int index, short value);

	/**
	 * Sets value in the array.
	 * Throws VM exception if array is null
	 * or index is out of bounds.
	 *
	 * @param array Array to set value in.
	 * @param index Value index.
	 * @param value Value to set.
	 */
	void arrayStoreByte(ObjectValue array, int index, byte value);

	/**
	 * Casts an object, throws
	 * VM exception if cast failed.
	 *
	 * @param value Value to attempt to cast.
	 * @param klass Class to cast value to.
	 * @return Same value.
	 */
	ObjectValue checkCast(ObjectValue value, JavaClass klass);

	/**
	 * Throws VM exception.
	 *
	 * @param value Exception to throw.
	 */
	void throwException(ObjectValue value);

	/**
	 * Sets reference value in an instance.
	 * Throws VM exception if field was not found,
	 * or an instance is {@code null}.
	 *
	 * @param instance Instance to set value in.
	 * @param klass    Field base class.
	 * @param name     Field name.
	 * @param desc     Field descriptor.
	 * @param value    Value to set.
	 */
	void putReference(ObjectValue instance, InstanceJavaClass klass, String name, String desc, ObjectValue value);

	/**
	 * Sets reference value in an instance.
	 * Throws VM exception if field was not found,
	 * or an instance is {@code null}.
	 *
	 * @param instance Instance to set value in.
	 * @param name     Field name.
	 * @param desc     Field descriptor.
	 * @param value    Value to set.
	 */
	void putReference(ObjectValue instance, String name, String desc, ObjectValue value);

	/**
	 * Sets long value in an instance.
	 * Throws VM exception if field was not found,
	 * or an instance is {@code null}.
	 *
	 * @param instance Instance to set value in.
	 * @param klass    Field base class.
	 * @param name     Field name.
	 * @param value    Value to set.
	 */
	void putLong(ObjectValue instance, InstanceJavaClass klass, String name, long value);

	/**
	 * Sets long value in an instance.
	 * Throws VM exception if field was not found,
	 * or an instance is {@code null}.
	 *
	 * @param instance Instance to set value in.
	 * @param name     Field name.
	 * @param value    Value to set.
	 */
	void putLong(ObjectValue instance, String name, long value);

	/**
	 * Sets double value in an instance.
	 * Throws VM exception if field was not found,
	 * or an instance is {@code null}.
	 *
	 * @param instance Instance to set value in.
	 * @param klass    Field base class.
	 * @param name     Field name.
	 * @param value    Value to set.
	 */
	void putDouble(ObjectValue instance, InstanceJavaClass klass, String name, double value);

	/**
	 * Sets double value in an instance.
	 * Throws VM exception if field was not found,
	 * or an instance is {@code null}.
	 *
	 * @param instance Instance to set value in.
	 * @param name     Field name.
	 * @param value    Value to set.
	 */
	void putDouble(ObjectValue instance, String name, double value);

	/**
	 * Sets int value in an instance.
	 * Throws VM exception if field was not found,
	 * or an instance is {@code null}.
	 *
	 * @param instance Instance to set value in.
	 * @param klass    Field base class.
	 * @param name     Field name.
	 * @param value    Value to set.
	 */
	void putInt(ObjectValue instance, InstanceJavaClass klass, String name, int value);

	/**
	 * Sets int value in an instance.
	 * Throws VM exception if field was not found,
	 * or an instance is {@code null}.
	 *
	 * @param instance Instance to set value in.
	 * @param name     Field name.
	 * @param value    Value to set.
	 */
	void putInt(ObjectValue instance, String name, int value);

	/**
	 * Sets float value in an instance.
	 * Throws VM exception if field was not found,
	 * or an instance is {@code null}.
	 *
	 * @param instance Instance to set value in.
	 * @param klass    Field base class.
	 * @param name     Field name.
	 * @param value    Value to set.
	 */
	void putFloat(ObjectValue instance, InstanceJavaClass klass, String name, float value);

	/**
	 * Sets float value in an instance.
	 * Throws VM exception if field was not found,
	 * or an instance is {@code null}.
	 *
	 * @param instance Instance to set value in.
	 * @param name     Field name.
	 * @param value    Value to set.
	 */
	void putFloat(ObjectValue instance, String name, float value);

	/**
	 * Sets char value in an instance.
	 * Throws VM exception if field was not found,
	 * or an instance is {@code null}.
	 *
	 * @param instance Instance to set value in.
	 * @param klass    Field base class.
	 * @param name     Field name.
	 * @param value    Value to set.
	 */
	void putChar(ObjectValue instance, InstanceJavaClass klass, String name, char value);

	/**
	 * Sets char value in an instance.
	 * Throws VM exception if field was not found,
	 * or an instance is {@code null}.
	 *
	 * @param instance Instance to set value in.
	 * @param name     Field name.
	 * @param value    Value to set.
	 */
	void putChar(ObjectValue instance, String name, char value);

	/**
	 * Sets short value in an instance.
	 * Throws VM exception if field was not found,
	 * or an instance is {@code null}.
	 *
	 * @param instance Instance to set value in.
	 * @param klass    Field base class.
	 * @param name     Field name.
	 * @param value    Value to set.
	 */
	void putShort(ObjectValue instance, InstanceJavaClass klass, String name, short value);

	/**
	 * Sets short value in an instance.
	 * Throws VM exception if field was not found,
	 * or an instance is {@code null}.
	 *
	 * @param instance Instance to set value in.
	 * @param name     Field name.
	 * @param value    Value to set.
	 */
	void putShort(ObjectValue instance, String name, short value);

	/**
	 * Sets byte value in an instance.
	 * Throws VM exception if field was not found,
	 * or an instance is {@code null}.
	 *
	 * @param instance Instance to set value in.
	 * @param klass    Field base class.
	 * @param name     Field name.
	 * @param value    Value to set.
	 */
	void putByte(ObjectValue instance, InstanceJavaClass klass, String name, byte value);

	/**
	 * Sets byte value in an instance.
	 * Throws VM exception if field was not found,
	 * or an instance is {@code null}.
	 *
	 * @param instance Instance to set value in.
	 * @param name     Field name.
	 * @param value    Value to set.
	 */
	void putByte(ObjectValue instance, String name, byte value);

	/**
	 * Sets boolean value in an instance.
	 * Throws VM exception if field was not found,
	 * or an instance is {@code null}.
	 *
	 * @param instance Instance to set value in.
	 * @param klass    Field base class.
	 * @param name     Field name.
	 * @param value    Value to set.
	 */
	void putBoolean(ObjectValue instance, InstanceJavaClass klass, String name, boolean value);

	/**
	 * Sets boolean value in an instance.
	 * Throws VM exception if field was not found,
	 * or an instance is {@code null}.
	 *
	 * @param instance Instance to set value in.
	 * @param name     Field name.
	 * @param value    Value to set.
	 */
	void putBoolean(ObjectValue instance, String name, boolean value);

	/**
	 * Gets reference value from an instance.
	 * Throws VM exception if field was not found,
	 * or an instance is {@code null}.
	 *
	 * @param instance Instance to get value from.
	 * @param klass    Field base class.
	 * @param name     Field name.
	 * @param desc     Field desc.
	 * @return field value.
	 */
	ObjectValue getReference(ObjectValue instance, InstanceJavaClass klass, String name, String desc);

	/**
	 * Gets reference value from an instance.
	 * Throws VM exception if field was not found,
	 * or an instance is {@code null}.
	 *
	 * @param instance Instance to get value from.
	 * @param name     Field name.
	 * @param desc     Field desc.
	 * @return field value.
	 */
	ObjectValue getReference(ObjectValue instance, String name, String desc);

	/**
	 * Gets long value from an instance.
	 * Throws VM exception if field was not found,
	 * or an instance is {@code null}.
	 *
	 * @param instance Instance to get value from.
	 * @param klass    Field base class.
	 * @param name     Field name.
	 * @return field value.
	 */
	long getLong(ObjectValue instance, InstanceJavaClass klass, String name);

	/**
	 * Gets long value from an instance.
	 * Throws VM exception if field was not found,
	 * or an instance is {@code null}.
	 *
	 * @param instance Instance to get value from.
	 * @param name     Field name.
	 * @return field value.
	 */
	long getLong(ObjectValue instance, String name);

	/**
	 * Gets double value from an instance.
	 * Throws VM exception if field was not found,
	 * or an instance is {@code null}.
	 *
	 * @param instance Instance to get value from.
	 * @param klass    Field base class.
	 * @param name     Field name.
	 * @return field value.
	 */
	double getDouble(ObjectValue instance, InstanceJavaClass klass, String name);

	/**
	 * Gets double value from an instance.
	 * Throws VM exception if field was not found,
	 * or an instance is {@code null}.
	 *
	 * @param instance Instance to get value from.
	 * @param name     Field name.
	 * @return field value.
	 */
	double getDouble(ObjectValue instance, String name);

	/**
	 * Gets int value from an instance.
	 * Throws VM exception if field was not found,
	 * or an instance is {@code null}.
	 *
	 * @param instance Instance to get value from.
	 * @param klass    Field base class.
	 * @param name     Field name.
	 * @return field value.
	 */
	int getInt(ObjectValue instance, InstanceJavaClass klass, String name);

	/**
	 * Gets int value from an instance.
	 * Throws VM exception if field was not found,
	 * or an instance is {@code null}.
	 *
	 * @param instance Instance to get value from.
	 * @param name     Field name.
	 * @return field value.
	 */
	int getInt(ObjectValue instance, String name);

	/**
	 * Gets float value from an instance.
	 * Throws VM exception if field was not found,
	 * or an instance is {@code null}.
	 *
	 * @param instance Instance to get value from.
	 * @param klass    Field base class.
	 * @param name     Field name.
	 * @return field value.
	 */
	float getFloat(ObjectValue instance, InstanceJavaClass klass, String name);

	/**
	 * Gets float value from an instance.
	 * Throws VM exception if field was not found,
	 * or an instance is {@code null}.
	 *
	 * @param instance Instance to get value from.
	 * @param name     Field name.
	 * @return field value.
	 */
	float getFloat(ObjectValue instance, String name);

	/**
	 * Gets char value from an instance.
	 * Throws VM exception if field was not found,
	 * or an instance is {@code null}.
	 *
	 * @param instance Instance to get value from.
	 * @param klass    Field base class.
	 * @param name     Field name.
	 * @return field value.
	 */
	char getChar(ObjectValue instance, InstanceJavaClass klass, String name);

	/**
	 * Gets char value from an instance.
	 * Throws VM exception if field was not found,
	 * or an instance is {@code null}.
	 *
	 * @param instance Instance to get value from.
	 * @param name     Field name.
	 * @return field value.
	 */
	char getChar(ObjectValue instance, String name);

	/**
	 * Gets short value from an instance.
	 * Throws VM exception if field was not found,
	 * or an instance is {@code null}.
	 *
	 * @param instance Instance to get value from.
	 * @param klass    Field base class.
	 * @param name     Field name.
	 * @return field value.
	 */
	short getShort(ObjectValue instance, InstanceJavaClass klass, String name);

	/**
	 * Gets short value from an instance.
	 * Throws VM exception if field was not found,
	 * or an instance is {@code null}.
	 *
	 * @param instance Instance to get value from.
	 * @param name     Field name.
	 * @return field value.
	 */
	short getShort(ObjectValue instance, String name);

	/**
	 * Gets byte value from an instance.
	 * Throws VM exception if field was not found,
	 * or an instance is {@code null}.
	 *
	 * @param instance Instance to get value from.
	 * @param klass    Field base class.
	 * @param name     Field name.
	 * @return field value.
	 */
	byte getByte(ObjectValue instance, InstanceJavaClass klass, String name);

	/**
	 * Gets byte value from an instance.
	 * Throws VM exception if field was not found,
	 * or an instance is {@code null}.
	 *
	 * @param instance Instance to get value from.
	 * @param name     Field name.
	 * @return field value.
	 */
	byte getByte(ObjectValue instance, String name);

	/**
	 * Gets boolean value from an instance.
	 * Throws VM exception if field was not found,
	 * or an instance is {@code null}.
	 *
	 * @param instance Instance to get value from.
	 * @param klass    Field base class.
	 * @param name     Field name.
	 * @return field value.
	 */
	boolean getBoolean(ObjectValue instance, InstanceJavaClass klass, String name);

	/**
	 * Gets boolean value from an instance.
	 * Throws VM exception if field was not found,
	 * or an instance is {@code null}.
	 *
	 * @param instance Instance to get value from.
	 * @param name     Field name.
	 * @return field value.
	 */
	boolean getBoolean(ObjectValue instance, String name);

	/**
	 * Gets static reference value from a class.
	 * Throws VM exception if field was not found.
	 *
	 * @param klass Class to get value from.
	 * @param name  Field name.
	 * @param desc  Field descriptor.
	 * @return field value.
	 */
	ObjectValue getReference(InstanceJavaClass klass, String name, String desc);

	/**
	 * Gets static long value from a class.
	 * Throws VM exception if field was not found.
	 *
	 * @param klass Class to get value from.
	 * @param name  Field name.
	 * @return field value.
	 */
	long getLong(InstanceJavaClass klass, String name);

	/**
	 * Gets static double value from a class.
	 * Throws VM exception if field was not found.
	 *
	 * @param klass Class to get value from.
	 * @param name  Field name.
	 * @return field value.
	 */
	double getDouble(InstanceJavaClass klass, String name);

	/**
	 * Gets static int value from a class.
	 * Throws VM exception if field was not found.
	 *
	 * @param klass Class to get value from.
	 * @param name  Field name.
	 * @return field value.
	 */
	int getInt(InstanceJavaClass klass, String name);

	/**
	 * Gets static float value from a class.
	 * Throws VM exception if field was not found.
	 *
	 * @param klass Class to get value from.
	 * @param name  Field name.
	 * @return field value.
	 */
	float getFloat(InstanceJavaClass klass, String name);

	/**
	 * Gets static char value from a class.
	 * Throws VM exception if field was not found.
	 *
	 * @param klass Class to get value from.
	 * @param name  Field name.
	 * @return field value.
	 */
	char getChar(InstanceJavaClass klass, String name);

	/**
	 * Gets static short value from a class.
	 * Throws VM exception if field was not found.
	 *
	 * @param klass Class to get value from.
	 * @param name  Field name.
	 * @return field value.
	 */
	short getShort(InstanceJavaClass klass, String name);

	/**
	 * Gets static byte value from a class.
	 * Throws VM exception if field was not found.
	 *
	 * @param klass Class to get value from.
	 * @param name  Field name.
	 * @return field value.
	 */
	byte getByte(InstanceJavaClass klass, String name);

	/**
	 * Gets static boolean value from a class.
	 * Throws VM exception if field was not found.
	 *
	 * @param klass Class to get value from.
	 * @param name  Field name.
	 * @return field value.
	 */
	boolean getBoolean(InstanceJavaClass klass, String name);

	/**
	 * Sets static reference value in a class.
	 * Throws VM exception if field was not found.
	 *
	 * @param klass Class to set value for.
	 * @param name  Field name.
	 * @param desc  Field descriptor.
	 * @param value Value to set.
	 */
	void putReference(InstanceJavaClass klass, String name, String desc, ObjectValue value);

	/**
	 * Sets static long value in a class.
	 * Throws VM exception if field was not found.
	 *
	 * @param klass Class to set value for.
	 * @param name  Field name.
	 * @param value Value to set.
	 */
	void putLong(InstanceJavaClass klass, String name, long value);

	/**
	 * Sets static double value in a class.
	 * Throws VM exception if field was not found.
	 *
	 * @param klass Class to set value for.
	 * @param name  Field name.
	 * @param value Value to set.
	 */
	void putDouble(InstanceJavaClass klass, String name, double value);

	/**
	 * Sets static int value in a class.
	 * Throws VM exception if field was not found.
	 *
	 * @param klass Class to set value for.
	 * @param name  Field name.
	 * @param value Value to set.
	 */
	void putInt(InstanceJavaClass klass, String name, int value);

	/**
	 * Sets static float value in a class.
	 * Throws VM exception if field was not found.
	 *
	 * @param klass Class to set value for.
	 * @param name  Field name.
	 * @param value Value to set.
	 */
	void putFloat(InstanceJavaClass klass, String name, float value);

	/**
	 * Sets static char value in a class.
	 * Throws VM exception if field was not found.
	 *
	 * @param klass Class to set value for.
	 * @param name  Field name.
	 * @param value Value to set.
	 */
	void putChar(InstanceJavaClass klass, String name, char value);

	/**
	 * Sets static short value in a class.
	 * Throws VM exception if field was not found.
	 *
	 * @param klass Class to set value for.
	 * @param name  Field name.
	 * @param value Value to set.
	 */
	void putShort(InstanceJavaClass klass, String name, short value);

	/**
	 * Sets static byte value in a class.
	 * Throws VM exception if field was not found.
	 *
	 * @param klass Class to set value for.
	 * @param name  Field name.
	 * @param value Value to set.
	 */
	void putByte(InstanceJavaClass klass, String name, byte value);

	/**
	 * Sets static boolean value in a class.
	 * Throws VM exception if field was not found.
	 *
	 * @param klass Class to set value for.
	 * @param name  Field name.
	 * @param value Value to set.
	 */
	void putBoolean(InstanceJavaClass klass, String name, boolean value);

	/**
	 * Attempts to unlock object monitor.
	 * Throws VM exception if an object is {@code null},
	 * or if current thread does not own the lock.
	 *
	 * @param value Object to unlock.
	 */
	void monitorExit(ObjectValue value);

	/**
	 * Performs {@code instanceof} check.
	 *
	 * @param value     Value to perform the check on.
	 * @param javaClass Target type.
	 * @return {@code true} if comparison is success.
	 */
	boolean instanceofCheck(ObjectValue value, JavaClass javaClass);
}
