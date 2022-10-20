package dev.xdark.ssvm.util;

import dev.xdark.ssvm.mirror.type.InstanceClass;
import dev.xdark.ssvm.mirror.type.JavaClass;
import dev.xdark.ssvm.value.ArrayValue;
import dev.xdark.ssvm.value.InstanceValue;
import dev.xdark.ssvm.value.ObjectValue;

/**
 * VM operations.
 *
 * @author xDark
 */
public interface Operations {

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
	 * Performs {@code instanceof} check.
	 *
	 * @param value     Value to perform the check on.
	 * @param javaClass Target type.
	 * @return {@code true} if comparison is success.
	 */
	boolean instanceofCheck(ObjectValue value, JavaClass javaClass);
}
