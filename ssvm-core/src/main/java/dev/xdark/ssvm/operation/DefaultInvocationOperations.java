package dev.xdark.ssvm.operation;

import dev.xdark.ssvm.execution.ExecutionEngine;
import dev.xdark.ssvm.execution.ExecutionRequest;
import dev.xdark.ssvm.execution.Locals;
import dev.xdark.ssvm.execution.Stack;
import dev.xdark.ssvm.mirror.member.JavaMethod;
import dev.xdark.ssvm.thread.ThreadManager;
import dev.xdark.ssvm.value.ObjectValue;
import dev.xdark.ssvm.value.sink.BlackholeValueSink;
import dev.xdark.ssvm.value.sink.ReflectionSink;
import dev.xdark.ssvm.value.sink.ValueSink;
import lombok.RequiredArgsConstructor;

/**
 * Default implementation.
 *
 * @author xDark
 */
@RequiredArgsConstructor
public final class DefaultInvocationOperations implements InvocationOperations {

	private final ExecutionEngine executionEngine;
	private final ThreadManager threadManager;

	@Override
	public <R extends ValueSink> R invoke(JavaMethod method, Locals locals, R sink) {
		SimpleExecutionRequest<R> request = new SimpleExecutionRequest<>();
		request.init(method, threadManager.currentThreadStorage().newStack(method), locals, sink);
		executionEngine.execute(request);
		return sink;
	}

	@Override
	public void invokeVoid(JavaMethod method, Locals locals) {
		invoke(method, locals, BlackholeValueSink.INSTANCE);
	}

	@Override
	public ObjectValue invokeReference(JavaMethod method, Locals locals) {
		return invoke(method, locals, new ReflectionSink()).referenceValue;
	}

	@Override
	public long invokeLong(JavaMethod method, Locals locals) {
		return invoke(method, locals, new ReflectionSink()).longValue;
	}

	@Override
	public double invokeDouble(JavaMethod method, Locals locals) {
		return Double.longBitsToDouble(invokeLong(method, locals));
	}

	@Override
	public int invokeInt(JavaMethod method, Locals locals) {
		return invoke(method, locals, new ReflectionSink()).intValue;
	}

	@Override
	public float invokeFloat(JavaMethod method, Locals locals) {
		return Float.intBitsToFloat(invokeInt(method, locals));
	}

	@Override
	public short invokeShort(JavaMethod method, Locals locals) {
		return (short) invokeInt(method, locals);
	}

	@Override
	public char invokeChar(JavaMethod method, Locals locals) {
		return (char) invokeInt(method, locals);
	}

	@Override
	public byte invokeByte(JavaMethod method, Locals locals) {
		return (byte) invokeInt(method, locals);
	}

	@Override
	public boolean invokeBoolean(JavaMethod method, Locals locals) {
		return invokeInt(method, locals) != 0;
	}

	private static final class SimpleExecutionRequest<R extends ValueSink> implements ExecutionRequest<R> {
		private JavaMethod method;
		private Stack stack;
		private Locals locals;
		private R resultSink;

		SimpleExecutionRequest() {
		}

		void init(JavaMethod method, Stack stack, Locals locals, R resultSink) {
			this.method = method;
			this.stack = stack;
			this.locals = locals;
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
		public R getResultSink() {
			return resultSink;
		}
	}

}
