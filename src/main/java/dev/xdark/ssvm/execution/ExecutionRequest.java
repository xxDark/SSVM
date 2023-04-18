package dev.xdark.ssvm.execution;

import dev.xdark.ssvm.mirror.JavaMethod;
import dev.xdark.ssvm.value.sink.ValueSink;

/**
 * VM execution request.
 *
 * @author xDark
 */
public interface ExecutionRequest<R extends ValueSink> {

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
	 * @return execution options.
	 */
	ExecutionOptions getOptions();

	/**
	 * @return Result sink.
	 */
	R getResultSink();
}
