package dev.xdark.ssvm.jvmti.event;

import dev.xdark.ssvm.execution.ExecutionContext;

/**
 * Fired when JVM enters a method.
 *
 * @author xDark
 */
@FunctionalInterface
public interface MethodEnter {

	/**
	 * @param ctx Execution context.
	 */
	void invoke(ExecutionContext<?> ctx);
}
