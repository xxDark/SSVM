package dev.xdark.ssvm.execution;

import dev.xdark.ssvm.mirror.member.JavaMethod;
import dev.xdark.ssvm.thread.backtrace.StackFrame;
import dev.xdark.ssvm.util.Disposable;
import dev.xdark.ssvm.util.DisposeUtil;
import dev.xdark.ssvm.value.ObjectValue;
import dev.xdark.ssvm.value.sink.ValueSink;


public final class SimpleExecutionContext<R extends ValueSink> implements ExecutionContext<R>, Disposable {

	private final EngineReference<ExecutionContext<?>> engineReference;
	private JavaMethod method;
	private Stack stack;
	private Locals locals;
	private R sink;
	private int insnPosition;
	private int lineNumber = -1;
	final StackFrame frame = new SimpleStackFrame(this);

	/**
	 * @param stack  Execution stack.
	 * @param locals Local variable table.
	 * @param sink   Value sink, where the result will be put.
	 */
	public SimpleExecutionContext(JavaMethod method, Stack stack, Locals locals, R sink) {
		engineReference = null;
		init(method, stack, locals, sink);
	}

	SimpleExecutionContext(EngineReference<ExecutionContext<?>> engineReference) {
		this.engineReference = engineReference;
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
	public int getInsnPosition() {
		return insnPosition;
	}

	@Override
	public void setInsnPosition(int insnPosition) {
		this.insnPosition = insnPosition;
	}

	@Override
	public int getLineNumber() {
		return lineNumber;
	}

	@Override
	public void setLineNumber(int lineNumber) {
		this.lineNumber = lineNumber;
	}

	@Override
	public R returnSink() {
		return sink;
	}

	@Override
	public void setResult(ObjectValue result) {
		sink.acceptReference(result);
	}

	@Override
	public void setResult(long result) {
		sink.acceptLong(result);
	}

	@Override
	public void setResult(double result) {
		sink.acceptDouble(result);
	}

	@Override
	public void setResult(int result) {
		sink.acceptInt(result);
	}

	@Override
	public void setResult(float result) {
		sink.acceptFloat(result);
	}

	@Override
	public void dispose() {
		DisposeUtil.dispose(stack);
		DisposeUtil.dispose(locals);
		engineReference.recycle(this);
	}

	void init(JavaMethod method, Stack stack, Locals locals, R sink) {
		this.method = method;
		this.stack = stack;
		this.locals = locals;
		this.sink = sink;
		insnPosition = 0;
		lineNumber = -1;
	}
}
