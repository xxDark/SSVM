package dev.xdark.ssvm.operation;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.mirror.type.InstanceClass;
import dev.xdark.ssvm.value.InstanceValue;
import dev.xdark.ssvm.value.ObjectValue;

/**
 * VM exception operations.
 *
 * @author xDark
 */
public interface ExceptionOperations {

	/**
	 * @param frame Stack frame.
	 * @return VM oop of {@link StackTraceElement}.
	 */
	InstanceValue newStackTraceElement(ExecutionContext<?> frame);

	/**
	 * Throws exception.
	 *
	 * @param value Exception to throw.
	 */
	void throwException(ObjectValue value);

	/**
	 * Creates new exception.
	 *
	 * @param javaClass Exception class.
	 * @param message   Exception message.
	 * @param cause     Exception cause.
	 * @return new exception instance.
	 */
	InstanceValue newException(InstanceClass javaClass, String message, ObjectValue cause);

	/**
	 * Creates new exception.
	 *
	 * @param javaClass Exception class.
	 * @param message   Exception message.
	 * @return new exception instance.
	 */
	default InstanceValue newException(InstanceClass javaClass, String message) {
		return newException(javaClass, message, null);
	}

	/**
	 * Creates new exception.
	 *
	 * @param javaClass Exception class.
	 * @param cause     Exception cause.
	 * @return new exception instance.
	 */
	default InstanceValue newException(InstanceClass javaClass, ObjectValue cause) {
		return newException(javaClass, null, cause);
	}

	/**
	 * Creates new exception.
	 *
	 * @param javaClass Exception class.
	 * @return new exception instance.
	 */
	default InstanceValue newException(InstanceClass javaClass) {
		return newException(javaClass, null, null);
	}

	/**
	 * Throws exception.
	 *
	 * @param javaClass Exception class.
	 * @param message   Message.
	 * @param cause     Exception cause.
	 */
	default void throwException(InstanceClass javaClass, String message, ObjectValue cause) {
		throwException(newException(javaClass, message, cause));
	}

	/**
	 * Throws exception.
	 *
	 * @param javaClass Exception class.
	 * @param message   Message.
	 */
	default void throwException(InstanceClass javaClass, String message) {
		throwException(javaClass, message, null);
	}

	/**
	 * Throws exception.
	 *
	 * @param javaClass Exception class.
	 * @param cause     Exception cause.
	 */
	default void throwException(InstanceClass javaClass, ObjectValue cause) {
		throwException(javaClass, null, cause);
	}

	/**
	 * Throws exception.
	 *
	 * @param javaClass Exception class.
	 */
	default void throwException(InstanceClass javaClass) {
		throwException(javaClass, null, null);
	}
}
