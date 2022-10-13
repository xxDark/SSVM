package dev.xdark.ssvm.thread.backtrace;

import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.ExecutionRequest;
import dev.xdark.ssvm.value.sink.ValueSink;

/**
 * Thread backtrace.
 *
 * @author xDark
 */
public interface Backtrace extends Iterable<ExecutionContext<?>> {

	/**
	 * Allocates new stack frame and pushes
	 * it to the top.
	 *
	 * @param request Execution request.
	 * @return New frame.
	 */
	<R extends ValueSink> ExecutionContext<R> push(ExecutionRequest<R> request);

	/**
	 * @return Current frame.
	 */
	ExecutionContext<?> peek();

	/**
	 * @param index Frame index.
	 * @return Stack frame.
	 */
	ExecutionContext<?> at(int index);

	/**
	 * Pops current frame.
	 */
	void pop();

	/**
	 * @return Backtrace depth.
	 */
	int depth();
}
