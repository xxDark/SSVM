package dev.xdark.ssvm.execution;

import dev.xdark.ssvm.thread.ThreadRegion;
import dev.xdark.ssvm.thread.SimpleThreadStorage;
import dev.xdark.ssvm.util.Disposable;
import dev.xdark.ssvm.value.TopValue;
import dev.xdark.ssvm.value.Value;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Method execution stack
 * that uses thread local storage.
 *
 * @author xDark
 */
@RequiredArgsConstructor
public final class ThreadStack implements Stack, AutoCloseable, Disposable {

	private final ThreadRegion stack;
	private int cursor;

	/**
	 * @param maxSize The maximum size of the stack.
	 */
	public ThreadStack(int maxSize) {
		stack = SimpleThreadStorage.threadPush(maxSize);
	}

	@Override
	public void push(Value value) {
		checkValue(value);
		if (value.isWide()) {
			throw new IllegalStateException("Must use pushWide instead");
		}
		stack.set(cursor++, value);
	}

	@Override
	public void pushWide(Value value) {
		checkValue(value);
		if (!value.isWide()) {
			throw new IllegalStateException("Must use push instead");
		}
		ThreadRegion stack = this.stack;
		int cursor = this.cursor;
		stack.set(cursor++, value);
		stack.set(cursor++, TopValue.INSTANCE);
		this.cursor = cursor;
	}

	@Override
	public void pushGeneric(Value value) {
		if (value.isWide()) {
			pushWide(value);
		} else {
			push(value);
		}
	}

	@Override
	public void pushRaw(Value value) {
		stack.set(cursor++, value);
	}

	@Override
	public <V extends Value> V pop() {
		return (V) stack.get(--cursor);
	}

	@Override
	public <V extends Value> V popWide() {
		Value top = pop();
		if (top != TopValue.INSTANCE) {
			throw new IllegalStateException("Expected to pop TOP value, but got: " + top);
		}
		return pop();
	}

	@Override
	public <V extends Value> V popGeneric() {
		Value top = pop();
		if (top == TopValue.INSTANCE) {
			return pop();
		}
		return (V) top;
	}

	@Override
	public <V extends Value> V peek() {
		return (V) stack.get(cursor - 1);
	}

	@Override
	public <V extends Value> V poll() {
		if (cursor == 0) {
			return null;
		}
		return (V) stack.get(--cursor);
	}

	@Override
	public void dup() {
		ThreadRegion stack = this.stack;
		int cursor = this.cursor;
		stack.set(this.cursor++, stack.get(cursor - 1));
	}

	@Override
	public void swap() {
		ThreadRegion stack = this.stack;
		int cursor = this.cursor;
		Value v1 = stack.get(cursor - 1);
		Value v2 = stack.get(cursor - 2);
		stack.set(cursor - 1, v2);
		stack.set(cursor - 2, v1);
	}

	@Override
	public boolean isEmpty() {
		return cursor == 0;
	}

	@Override
	public void clear() {
		cursor = 0;
	}

	@Override
	public int position() {
		return cursor;
	}

	@Override
	public <V extends Value> V getAt(int index) {
		return (V) stack.get(index);
	}

	@Override
	public List<Value> view() {
		return Arrays.asList(Arrays.copyOf(stack.unwrap(), cursor));
	}

	@Override
	public void sinkInto(Locals locals, int count) {
		if (count == 0) {
			return;
		}
		ThreadRegion stack = this.stack;
		int start = cursor - count;
		locals.copyFrom(stack.getArray(), stack.map(start), 0, count);
		cursor = start;
	}

	@Override
	public void dispose() {
		stack.dispose();
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		if (!(o instanceof Stack)) {
			return false;
		}
		Stack other = (Stack) o;
		int cursor = this.cursor;
		if (cursor != other.position()) {
			return false;
		}
		for (int i = 0; i < cursor; i++) {
			if (!Objects.equals(getAt(i), other.getAt(i))) {
				return false;
			}
		}
		return true;
	}

	@Override
	public int hashCode() {
		int result = 1;
		int cursor = this.cursor;
		ThreadRegion stack = this.stack;
		for (int i = 0; i < cursor; i++) {
			result *= 31;
			result += Objects.hashCode(stack.get(i));
		}
		return result;
	}

	@Override
	public String toString() {
		return "Stack{" +
			"stack=" + Arrays.toString(stack.unwrap()) +
			", cursor=" + cursor +
			'}';
	}

	private static void checkValue(Value value) {
		if (Objects.requireNonNull(value, "value").isVoid()) {
			throw new IllegalStateException("Cannot push void value");
		}
	}
}
