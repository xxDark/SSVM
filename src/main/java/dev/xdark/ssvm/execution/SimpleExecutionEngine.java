package dev.xdark.ssvm.execution;

import dev.xdark.ssvm.mirror.JavaMethod;

/**
 * Simple execution engine.
 *
 * @author xDark
 */
public class SimpleExecutionEngine implements ExecutionEngine {

	@Override
	public ExecutionContext createContext(JavaMethod method, Stack stack, Locals locals) {
		return new SimpleExecutionContext(method, stack, locals);
	}
}
