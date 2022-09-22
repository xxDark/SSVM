package dev.xdark.ssvm.thread.backtrace;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.mirror.type.InstanceJavaClass;

/**
 * VM stack frame information.
 *
 * @author xDark
 */
public interface StackFrame {

	/**
	 * Returns the class containing
	 * the execution point represented
	 * by the stack frame.
	 *
	 * @return declaring class.
	 */
	InstanceJavaClass getDeclaringClass();

	/**
	 * Returns name of
	 * the method containing the execution point
	 * by the stack frame.
	 *
	 * @return method name.
	 */
	String getMethodName();

	/**
	 * Returns the name of the file containing
	 * the execution point represented
	 * by the stack frame.
	 *
	 * @return source file or {@code null}, if unavailable.
	 */
	String getSourceFile();

	/**
	 * Returns line number of
	 * the method containing the execution point
	 * by the stack frame.
	 *
	 * @return line number.
	 */
	int getLineNumber();

	/**
	 * Returns execution context of
	 * the method containing the execution point
	 * by the stack frame.
	 *
	 * @return execution context or {@code null},
	 * if no context is present.
	 */
	ExecutionContext<?> getExecutionContext();

	/**
	 * @return immutable frame.
	 */
	StackFrame freeze();

	/**
	 * Creates stack frame from the execution point.
	 *
	 * @param ctx Execution context.
	 * @return stack frame that mirrors it's information from
	 * execution context.
	 */
	static StackFrame ofContext(ExecutionContext<?> ctx) {
		return new ContextStackFrame(ctx);
	}

	/**
	 * Creates new stack frame.
	 *
	 * @param declaringClass the class containing
	 *                       the execution point represented
	 *                       by the stack frame.
	 * @param methodName     name of
	 *                       the method containing the execution point
	 *                       by the stack frame.
	 * @param sourceFile     name of the file containing
	 *                       the execution point represented
	 *                       by the stack frame.
	 * @param lineNumber     line number of
	 *                       the method containing the execution point
	 *                       by the stack frame.
	 * @return new stack frame.
	 */
	static StackFrame from(InstanceJavaClass declaringClass, String methodName, String sourceFile, int lineNumber) {
		return new ContextlessStackFrame(declaringClass, methodName, sourceFile, lineNumber);
	}
}
