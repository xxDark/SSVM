package dev.xdark.ssvm.execution;

import dev.xdark.ssvm.mirror.JavaMethod;

/**
 * Basic execution request.
 *
 * @author xDark
 */
public final class SimpleExecutionRequest implements ExecutionRequest {
	private final JavaMethod method;
	private final Stack stack;
	private final Locals locals;
	private final ExecutionOptions options;

	/**
	 * @param method  method to execute.
	 * @param stack   pre-allocated stack.
	 * @param locals  pre-allocated locals.
	 * @param options execution options.
	 */
	public SimpleExecutionRequest(JavaMethod method, Stack stack, Locals locals, ExecutionOptions options) {
		this.method = method;
		this.stack = stack;
		this.locals = locals;
		this.options = options;
	}

	@Override
	public JavaMethod getMethod() {
		return method;
	}

	@Override
	public Stack getStack() {
		return stack;
	}

	@Override
	public Locals getLocals() {
		return locals;
	}

	@Override
	public ExecutionOptions getOptions() {
		return options;
	}
}
