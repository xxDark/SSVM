package dev.xdark.ssvm.thread;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.value.InstanceValue;

/**
 * VM thread manager.
 *
 * @author xDark
 */
public interface ThreadManager {

	/**
	 * Returns VM thread from Java thread.
	 *
	 * @param thread
	 * 		Thread to get VM thread from.
	 *
	 * @return VM thread.
	 */
	VMThread getVmThread(Thread thread);

	/**
	 * Returns VMThread by an instance.
	 *
	 * @param thread
	 * 		VM thread oop.
	 */
	VMThread getVmThread(InstanceValue thread);

	/**
	 * Assigns VM thread.
	 *
	 * @param thread
	 * 		Thread to assign.
	 */
	void setVmThread(VMThread thread);

	/**
	 * Creates new stack frame.
	 *
	 * @param declaringClass
	 * 		the class containing
	 * 		the execution point represented
	 * 		by the stack frame.
	 * @param methodName
	 * 		name of
	 * 		the method containing the execution point
	 * 		by the stack frame.
	 * @param sourceFile
	 * 		name of the file containing
	 * 		the execution point represented
	 * 		by the stack frame.
	 * @param lineNumber
	 * 		line number of
	 * 		the method containing the execution point
	 * 		by the stack frame.
	 *
	 * @return new stack frame.
	 */
	StackFrame newStackFrame(InstanceJavaClass declaringClass, String methodName, String sourceFile, int lineNumber);

	/**
	 * Creates new stack frame.
	 *
	 * @param context
	 * 		Context holding an information about the frame.
	 *
	 * @return stack frame.
	 */
	StackFrame newStackFrame(ExecutionContext context);

	/**
	 * Returns current VM thread.
	 *
	 * @return current VM thread.
	 */
	default VMThread currentThread() {
		return getVmThread(Thread.currentThread());
	}
}
