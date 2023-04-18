package dev.xdark.ssvm.jit;

import dev.xdark.ssvm.execution.ExecutionContext;

/**
 * Invoker for compiled code.
 *
 * @author xDark
 */
@Deprecated
public abstract class AbstractInvoker {

	/**
	 * @param ctx Processing content.
	 */
	public abstract void execute(ExecutionContext<?> ctx);
}
