package dev.xdark.ssvm.thread.backtrace;

import dev.xdark.ssvm.execution.Locals;
import dev.xdark.ssvm.execution.Stack;
import dev.xdark.ssvm.mirror.member.JavaMethod;
import dev.xdark.ssvm.util.DisposeUtil;
import dev.xdark.ssvm.value.sink.ValueSink;

import java.lang.reflect.Modifier;

/**
 * Simple stack frame.
 *
 * @author xDark
 */
public final class SimpleStackFrame<R extends ValueSink> implements StackFrame<R> {

	private JavaMethod method;
	private Locals locals;
	private Stack stack;
	private R returnSink;
	private int lineNumber;
	private int pc;

	void init(JavaMethod method, Locals locals, Stack stack, R returnSink) {
		this.method = method;
		this.locals = locals;
		this.stack = stack;
		this.returnSink = returnSink;
		lineNumber = Modifier.isNative(method.getModifiers()) ? -1 : -2;
		pc = 0;
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
		return pc;
	}

	@Override
	public void setInsnPosition(int position) {
		pc = position;
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
		return returnSink;
	}

	@Override
	public void dispose() {
		DisposeUtil.dispose(locals);
		DisposeUtil.dispose(stack);
	}
}
