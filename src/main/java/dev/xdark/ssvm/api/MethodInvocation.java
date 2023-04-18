package dev.xdark.ssvm.api;

import dev.xdark.ssvm.execution.ExecutionContext;

/**
 * Called on method enter/exit.
 *
 * @author xDark
 */
public interface MethodInvocation {

	/**
	 * Handles method enter/exit.
	 *
	 * @param ctx Context of the method being executed.
	 */
	void handle(ExecutionContext ctx);
}
