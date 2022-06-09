package dev.xdark.ssvm.execution;

import dev.xdark.ssvm.mirror.JavaMethod;

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
	public void execute(ExecutionContext ctx, ExecutionContextOptions options) {
		delegate.execute(ctx, options);
	}

	@Override
	public ExecutionContext createContext(JavaMethod method, Stack stack, Locals locals) {
		return delegate.createContext(method, stack, locals);
	}

	@Override
	public ExecutionContextOptions defaultOptions() {
		return delegate.defaultOptions();
	}
}
