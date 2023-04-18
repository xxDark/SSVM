package dev.xdark.ssvm.execution;

import dev.xdark.ssvm.value.sink.ValueSink;

/**
 * VM execution engine.
 *
 * @author xDark
 */
public interface ExecutionEngine {

	/**
	 * Executes the context.
	 *
	 * @param ctx     Context to execute.
	 */
	<R extends ValueSink> ExecutionContext<R> execute(ExecutionRequest<R> ctx);
}
