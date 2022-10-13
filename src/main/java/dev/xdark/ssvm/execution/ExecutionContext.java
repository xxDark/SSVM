package dev.xdark.ssvm.execution;

import dev.xdark.ssvm.mirror.member.JavaMethod;
import dev.xdark.ssvm.value.ObjectValue;
import dev.xdark.ssvm.value.sink.ValueSink;

/**
 * Execution context of a method.
 *
 * @author xDark
 */
public interface ExecutionContext<R extends ValueSink> {

	/**
	 * @return Method being executed.
	 */
	JavaMethod getMethod();

	/**
	 * @return Frame stack.
	 */
	Stack getStack();

	/**
	 * @return Frame locals.
	 */
	Locals getLocals();

	/**
	 * @return Instruction position.
	 */
	int getInsnPosition();

	/**
	 * @param position
	 *      New instruction position.
	 */
	void setInsnPosition(int position);

	/**
	 * @return Current line number.
	 */
	int getLineNumber();

	/**
	 * @param lineNumber New line number.
	 */
	void setLineNumber(int lineNumber);

	/**
	 * @return Return sink.
	 */
	R returnSink();

	/**
	 * Sets execution result.
	 *
	 * @param result Value to set.
	 */
	default void setResult(ObjectValue result) {
		returnSink().acceptReference(result);
	}

	/**
	 * Sets execution result.
	 *
	 * @param result Value to set.
	 */
	default void setResult(long result) {
		returnSink().acceptLong(result);
	}

	/**
	 * Sets execution result.
	 *
	 * @param result Value to set.
	 */
	default void setResult(double result) {
		returnSink().acceptDouble(result);
	}

	/**
	 * Sets execution result.
	 *
	 * @param result Value to set.
	 */
	default void setResult(int result) {
		returnSink().acceptInt(result);
	}

	/**
	 * Sets execution result.
	 *
	 * @param result Value to set.
	 */
	default void setResult(float result) {
		returnSink().acceptFloat(result);
	}
}
