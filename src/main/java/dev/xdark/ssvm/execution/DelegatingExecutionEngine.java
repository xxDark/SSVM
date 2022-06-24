package dev.xdark.ssvm.execution;

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
	public void execute(ExecutionContext ctx) {
		delegate.execute(ctx);
	}

	@Override
	public ExecutionContext createContext(ExecutionRequest request) {
		return delegate.createContext(request);
	}

	@Override
	public ExecutionOptions defaultOptions() {
		return delegate.defaultOptions();
	}
}
