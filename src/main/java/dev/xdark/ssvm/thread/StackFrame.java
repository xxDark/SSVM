package dev.xdark.ssvm.thread;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.mirror.InstanceJavaClass;

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
	ExecutionContext getExecutionContext();
}
