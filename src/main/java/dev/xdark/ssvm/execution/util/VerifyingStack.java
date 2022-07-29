package dev.xdark.ssvm.execution.util;

import dev.xdark.ssvm.execution.Locals;
import dev.xdark.ssvm.execution.Stack;
import dev.xdark.ssvm.value.Value;

import java.util.List;
import java.util.Objects;

/**
 * Stack that verifies push/pop operations.
 *
 * @author xDark
 */
public final class VerifyingStack implements Stack {
	private final Stack delegate;

	/**
	 * @param delegate Backing stack.
	 */
	public VerifyingStack(Stack delegate) {
		this.delegate = delegate;
	}

	@Override
	public void push(Value value) {
		checkValue(value);
		if (value.isWide()) {
			throw new IllegalStateException("Must use pushWide instead");
		}
		delegate.push(value);
	}

	@Override
	public void pushWide(Value value) {
		checkValue(value);
		if (!value.isWide()) {
			throw new IllegalStateException("Must use push instead");
		}
		delegate.pushWide(value);
	}

	@Override
	public void pushGeneric(Value value) {
		checkValue(value);
		delegate.pushGeneric(value);
	}

	@Override
	public void pushRaw(Value value) {
		checkValue(value);
		delegate.pushRaw(value);
	}

	@Override
	public <V extends Value> V pop() {
		return delegate.pop();
	}

	@Override
	public <V extends Value> V popWide() {
		return delegate.popWide();
	}

	@Override
	public <V extends Value> V popGeneric() {
		return delegate.popGeneric();
	}

	@Override
	public <V extends Value> V peek() {
		return delegate.peek();
	}

	@Override
	public <V extends Value> V poll() {
		return delegate.poll();
	}

	@Override
	public void clear() {
		delegate.clear();
	}

	@Override
	public int position() {
		return 0;
	}

	@Override
	public <V extends Value> V getAt(int index) {
		return delegate.getAt(index);
	}

	@Override
	public List<Value> view() {
		return delegate.view();
	}

	@Override
	public void sinkInto(Locals locals, int count) {
		delegate.sinkInto(locals, count);
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof Stack && delegate.equals(obj);
	}

	@Override
	public int hashCode() {
		return delegate.hashCode();
	}

	private static void checkValue(Value value) {
		if (Objects.requireNonNull(value, "value").isVoid()) {
			throw new IllegalStateException("Cannot push void value");
		}
	}
}
