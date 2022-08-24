package dev.xdark.ssvm.execution;

import dev.xdark.ssvm.mirror.JavaMethod;
import dev.xdark.ssvm.value.sink.ValueSink;

/**
 * Basic execution request.
 *
 * @author xDark
 */
public final class SimpleExecutionRequest<R extends ValueSink> implements ExecutionRequest<R> {
	private final JavaMethod method;
	private final Stack stack;
	private final Locals locals;
	private final ExecutionOptions options;
	private final R resultSink;

	/**
	 * @param method     method to execute.
	 * @param stack      pre-allocated stack.
	 * @param locals     pre-allocated locals.
	 * @param options    execution options.
	 * @param resultSink Result sink.
	 */
	public SimpleExecutionRequest(JavaMethod method, Stack stack, Locals locals, ExecutionOptions options, R resultSink) {
		this.method = method;
		this.stack = stack;
		this.locals = locals;
		this.options = options;
		this.resultSink = resultSink;
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

	@Override
	public R getResultSink() {
		return resultSink;
	}
}
