package dev.xdark.ssvm.api;

import dev.xdark.ssvm.execution.ExecutionContext;

/**
 * Called on method exit.
 *
 * @author xDark
 * @see MethodEnterListener
 */
public interface MethodExitListener {

	/**
	 * Handles method exit.
	 *
	 * @param ctx Context of the method being executed.
	 */
	void handle(ExecutionContext<?> ctx);
}
