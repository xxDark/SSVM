package dev.xdark.ssvm.jit;

import dev.xdark.ssvm.execution.ExecutionContext;

/**
 * Invoker for compiled code.
 *
 * @author xDark
 */
abstract class AbstractInvoker {

	/**
	 * @param ctx Processing content.
	 */
	abstract void execute(ExecutionContext<?> ctx);
}
