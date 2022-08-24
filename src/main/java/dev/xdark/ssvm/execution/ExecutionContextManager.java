package dev.xdark.ssvm.execution;

import dev.xdark.ssvm.value.sink.ValueSink;

/**
 * Responsible for managing of {@link ExecutionContext}.
 *
 * @author xDark
 */
public interface ExecutionContextManager {

	/**
	 * Creates new execution context.
	 *
	 * @param request execution request.
	 * @return new execution context.
	 */
	<R extends ValueSink> ExecutionContext<R> createContext(ExecutionRequest<R> request);
}
