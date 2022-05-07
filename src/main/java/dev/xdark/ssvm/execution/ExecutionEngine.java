package dev.xdark.ssvm.execution;

import dev.xdark.ssvm.mirror.JavaMethod;

/**
 * VM execution engine.
 *
 * @author xDark
 */
public interface ExecutionEngine {

	/**
	 * Creates new execution context.
	 *
	 * @param method
	 * 		Method to execute.
	 * @param stack
	 * 		Method stack.
	 * @param locals
	 * 		Method locals.
	 *
	 * @return new execution context.
	 */
	ExecutionContext createContext(JavaMethod method, Stack stack, Locals locals);
}
