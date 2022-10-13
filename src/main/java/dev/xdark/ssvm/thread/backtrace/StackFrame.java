package dev.xdark.ssvm.thread.backtrace;

import dev.xdark.ssvm.execution.Locals;
import dev.xdark.ssvm.execution.Stack;
import dev.xdark.ssvm.mirror.member.JavaMethod;
import dev.xdark.ssvm.util.Disposable;
import dev.xdark.ssvm.value.ObjectValue;
import dev.xdark.ssvm.value.sink.ValueSink;

/**
 * Stack frame.
 *
 * @author xDark
 */
public interface StackFrame<R extends ValueSink> extends Disposable {

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
