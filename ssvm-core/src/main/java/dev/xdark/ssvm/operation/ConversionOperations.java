package dev.xdark.ssvm.operation;

import dev.xdark.ssvm.value.ArrayValue;
import dev.xdark.ssvm.value.ObjectValue;

/**
 * VM array conversion.
 *
 * @author xDark
 */
public interface ConversionOperations {

	/**
	 * Converts an array to {@code long[]} array.
	 *
	 * @param array Array to convert.
	 * @return native Java array.
	 */
	long[] toJavaLongs(ArrayValue array);

	/**
	 * Converts an array to {@code double[]} array.
	 *
	 * @param array Array to convert.
	 * @return native Java array.
	 */
	double[] toJavaDoubles(ArrayValue array);

	/**
	 * Converts an array to {@code int[]} array.
	 *
	 * @param array Array to convert.
	 * @return native Java array.
	 */
	int[] toJavaInts(ArrayValue array);

	/**
	 * Converts an array to {@code float[]} array.
	 *
	 * @param array Array to convert.
	 * @return native Java array.
	 */
	float[] toJavaFloats(ArrayValue array);

	/**
	 * Converts an array to {@code char[]} array.
	 *
	 * @param array Array to convert.
	 * @return native Java array.
	 */
	char[] toJavaChars(ArrayValue array);

	/**
	 * Converts an array to {@code short[]} array.
	 *
	 * @param array Array to convert.
	 * @return native Java array.
	 */
	short[] toJavaShorts(ArrayValue array);

	/**
	 * Converts an array to {@code byte[]} array.
	 *
	 * @param array Array to convert.
	 * @return native Java array.
	 */
	byte[] toJavaBytes(ArrayValue array);

	/**
	 * Converts an array to {@code boolean[]} array.
	 *
	 * @param array Array to convert.
	 * @return native Java array.
	 */
	boolean[] toJavaBooleans(ArrayValue array);

	/**
	 * Converts an array to {@code Value[]} array.
	 *
	 * @param array Array to convert.
	 * @return native Java array.
	 */
	ObjectValue[] toJavaValues(ArrayValue array);

	/**
	 * Converts Java array to VM array.
	 *
	 * @param array      Array to convert.
	 * @param startIndex The initial index of the range to be converted, inclusive.
	 * @param endIndex   The final index of the range to be converted, exclusive.
	 * @return VM array.
	 */
	ArrayValue toVMLongs(long[] array, int startIndex, int endIndex);

	/**
	 * Converts Java array to VM array.
	 *
	 * @param array Array to convert.
	 * @return VM array.
	 */
	ArrayValue toVMLongs(long[] array);

	/**
	 * Converts Java array to VM array.
	 *
	 * @param array      Array to convert.
	 * @param startIndex The initial index of the range to be converted, inclusive.
	 * @param endIndex   The final index of the range to be converted, exclusive.
	 * @return VM array.
	 */
	ArrayValue toVMDoubles(double[] array, int startIndex, int endIndex);

	/**
	 * Converts Java array to VM array.
	 *
	 * @param array Array to convert.
	 * @return VM array.
	 */
	ArrayValue toVMDoubles(double[] array);

	/**
	 * Converts Java array to VM array.
	 *
	 * @param array      Array to convert.
	 * @param startIndex The initial index of the range to be converted, inclusive.
	 * @param endIndex   The final index of the range to be converted, exclusive.
	 * @return VM array.
	 */
	ArrayValue toVMInts(int[] array, int startIndex, int endIndex);

	/**
	 * Converts Java array to VM array.
	 *
	 * @param array Array to convert.
	 * @return VM array.
	 */
	default ArrayValue toVMInts(int[] array) {
		return toVMInts(array, 0, array.length);
	}

	/**
	 * Converts Java array to VM array.
	 *
	 * @param array      Array to convert.
	 * @param startIndex The initial index of the range to be converted, inclusive.
	 * @param endIndex   The final index of the range to be converted, exclusive.
	 * @return VM array.
	 */
	ArrayValue toVMFloats(float[] array, int startIndex, int endIndex);

	/**
	 * Converts Java array to VM array.
	 *
	 * @param array Array to convert.
	 * @return VM array.
	 */
	ArrayValue toVMFloats(float[] array);

	/**
	 * Converts Java array to VM array.
	 *
	 * @param array      Array to convert.
	 * @param startIndex The initial index of the range to be converted, inclusive.
	 * @param endIndex   The final index of the range to be converted, exclusive.
	 * @return VM array.
	 */
	ArrayValue toVMChars(char[] array, int startIndex, int endIndex);

	/**
	 * Converts Java array to VM array.
	 *
	 * @param array Array to convert.
	 * @return VM array.
	 */
	ArrayValue toVMChars(char[] array);

	/**
	 * Converts Java array to VM array.
	 *
	 * @param array      Array to convert.
	 * @param startIndex The initial index of the range to be converted, inclusive.
	 * @param endIndex   The final index of the range to be converted, exclusive.
	 * @return VM array.
	 */
	ArrayValue toVMShorts(short[] array, int startIndex, int endIndex);

	/**
	 * Converts Java array to VM array.
	 *
	 * @param array Array to convert.
	 * @return VM array.
	 */
	ArrayValue toVMShorts(short[] array);

	/**
	 * Converts Java array to VM array.
	 *
	 * @param array      Array to convert.
	 * @param startIndex The initial index of the range to be converted, inclusive.
	 * @param endIndex   The final index of the range to be converted, exclusive.
	 * @return VM array.
	 */
	ArrayValue toVMBytes(byte[] array, int startIndex, int endIndex);

	/**
	 * Converts Java array to VM array.
	 *
	 * @param array Array to convert.
	 * @return VM array.
	 */
	ArrayValue toVMBytes(byte[] array);

	/**
	 * Converts Java array to VM array.
	 *
	 * @param array      Array to convert.
	 * @param startIndex The initial index of the range to be converted, inclusive.
	 * @param endIndex   The final index of the range to be converted, exclusive.
	 * @return VM array.
	 */
	ArrayValue toVMBooleans(boolean[] array, int startIndex, int endIndex);

	/**
	 * Converts Java array to VM array.
	 *
	 * @param array Array to convert.
	 * @return VM array.
	 */
	ArrayValue toVMBooleans(boolean[] array);

	/**
	 * Converts Java array to VM array.
	 *
	 * @param array      Array to convert.
	 * @param startIndex The initial index of the range to be converted, inclusive.
	 * @param endIndex   The final index of the range to be converted, exclusive.
	 * @return VM array.
	 */
	ArrayValue toVMReferences(ObjectValue[] array, int startIndex, int endIndex);

	/**
	 * Converts Java array to VM array.
	 *
	 * @param array Array to convert.
	 * @return VM array.
	 */
	ArrayValue toVMReferences(ObjectValue[] array);
}
