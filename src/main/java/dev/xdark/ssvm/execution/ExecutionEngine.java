package dev.xdark.ssvm.execution;

/**
 * VM execution engine.
 *
 * @author xDark
 */
public interface ExecutionEngine extends ExecutionContextManager {

	/**
	 * Executes the context.
	 *
	 * @param ctx     Context to execute.
	 * @param options Context execution options.
	 */
	void execute(ExecutionContext ctx, ExecutionContextOptions options);

	/**
	 * @return Default execution options.
	 */
	ExecutionContextOptions defaultOptions();
}
