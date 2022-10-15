package dev.xdark.ssvm.operation;

import dev.xdark.ssvm.mirror.type.ArrayClass;
import dev.xdark.ssvm.mirror.type.InstanceClass;
import dev.xdark.ssvm.mirror.type.JavaClass;
import dev.xdark.ssvm.value.ArrayValue;
import dev.xdark.ssvm.value.InstanceValue;

/**
 * VM allocation operations.
 *
 * @author xDark
 */
public interface AllocationOperations {

	/**
	 * Attempts to allocate new instance of a type of a class.
	 * Throws VM exception if allocation fails.
	 *
	 * @param klass Class to allocate an instance of.
	 * @return allocated instance.
	 * @see InstanceClass#canAllocateInstance()
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
	 * Allocates multi array.
	 *
	 * @param type    Array type.
	 * @param lengths Array containing length of each dimension.
	 * @return New array.
	 */
	ArrayValue allocateMultiArray(ArrayClass type, int[] lengths);
}
