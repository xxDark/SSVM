package dev.xdark.ssvm.thread.backtrace;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.Locals;
import dev.xdark.ssvm.execution.Stack;
import dev.xdark.ssvm.mirror.member.JavaMethod;
import dev.xdark.ssvm.util.CloseableUtil;
import dev.xdark.ssvm.util.SafeCloseable;
import dev.xdark.ssvm.value.ObjectValue;
import dev.xdark.ssvm.value.sink.ValueSink;


final class SimpleExecutionContext<R extends ValueSink> implements ExecutionContext<R>, SafeCloseable {

	private final VirtualMachine vm;
	private JavaMethod method;
	private Stack stack;
	private Locals locals;
	private R sink;
	private int insnPosition;
	private int lineNumber = -1;

	SimpleExecutionContext(VirtualMachine vm) {
		this.vm = vm;
	}

	@Override
	public VirtualMachine getVM() {
		return vm;
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
	public void close() {
		CloseableUtil.close(stack);
		CloseableUtil.close(locals);
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
