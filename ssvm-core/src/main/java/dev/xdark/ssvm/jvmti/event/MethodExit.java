package dev.xdark.ssvm.jvmti.event;

import dev.xdark.ssvm.execution.ExecutionContext;

/**
 * Fired when JVM exits a method.
 *
 * @author xDark
 */
@FunctionalInterface
public interface MethodExit {

	/**
	 * @param ctx Execution context.
	 */
	void invoke(ExecutionContext<?> ctx);
}
