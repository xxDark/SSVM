package dev.xdark.ssvm.jit;

import dev.xdark.ssvm.execution.ExecutionContext;

/**
 * Same as consumer, but made abstract
 * on purpose. Why? Because HotSpot.
 *
 * @author xDark
 */
public abstract class JitInvoker {

	/**
	 * Invokes the context.
	 *
	 * @param ctx Context to be invoked.
	 */
	public abstract void invoke(ExecutionContext ctx);
}
