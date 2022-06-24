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
	 */
	void execute(ExecutionContext ctx);

	/**
	 * @return Default execution options.
	 */
	ExecutionOptions defaultOptions();
}
