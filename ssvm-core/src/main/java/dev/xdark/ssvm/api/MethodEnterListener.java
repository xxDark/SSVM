package dev.xdark.ssvm.api;

import dev.xdark.ssvm.execution.ExecutionContext;

/**
 * Called on method enter.
 *
 * @author xDark
 * @see MethodExitListener
 */
public interface MethodEnterListener {

	/**
	 * Handles method enter.
	 *
	 * @param ctx Context of the method being executed.
	 */
	void handle(ExecutionContext<?> ctx);
}
