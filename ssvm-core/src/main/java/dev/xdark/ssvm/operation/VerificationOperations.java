package dev.xdark.ssvm.operation;

import dev.xdark.ssvm.mirror.type.JavaClass;
import dev.xdark.ssvm.value.ObjectValue;

/**
 * VM verification operations.
 *
 * @author xDark
 */
public interface VerificationOperations {

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
	 * Throws VM exception if {@code value == null}.
	 *
	 * @param value Value to check.
	 * @return Same value.
	 */
	<V extends ObjectValue> V checkNotNull(ObjectValue value);

	/**
	 * Throws VM exception if {@code index < 0 || index >= length}.
	 *
	 * @param index  Index in the array.
	 * @param length Array length.
	 */
	void arrayRangeCheck(int index, int length);

	/**
	 * Throws VM exception if {@code length < 0} is true.
	 *
	 * @param length Array length to verify.
	 */
	void arrayLengthCheck(int length);

	/**
	 * Performs equality check.
	 *
	 * @param a Left value.
	 * @param b Right value.
	 */
	void checkEquals(int a, int b);

	/**
	 * Performs equality check.
	 *
	 * @param a Left value.
	 * @param b Right value.
	 */
	void checkEquals(long a, long b);
}
