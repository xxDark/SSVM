package dev.xdark.ssvm.execution;

import dev.xdark.ssvm.value.sink.ValueSink;

/**
 * Delegating execution engine.
 *
 * @author xDark
 */
public class DelegatingExecutionEngine implements ExecutionEngine {

	private final ExecutionEngine delegate;

	/**
	 * @param delegate Delegating engine.
	 */
	public DelegatingExecutionEngine(ExecutionEngine delegate) {
		this.delegate = delegate;
	}

	@Override
	public <R extends ValueSink> ExecutionContext<R> execute(ExecutionRequest<R> ctx) {
		return delegate.execute(ctx);
	}
}
