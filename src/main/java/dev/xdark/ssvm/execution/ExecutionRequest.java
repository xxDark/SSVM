package dev.xdark.ssvm.execution;

import dev.xdark.ssvm.mirror.JavaMethod;

/**
 * VM execution request.
 *
 * @author xDark
 */
public interface ExecutionRequest {

	/**
	 * @return method to execute.
	 */
	JavaMethod getMethod();

	/**
	 * @return pre-allocated stack.
	 */
	Stack getStack();

	/**
	 * @return pre-allocated locals.
	 */
	Locals getLocals();

	/**
	 * @return execution optinos.
	 */
	ExecutionOptions getOptions();
}
