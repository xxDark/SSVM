package dev.xdark.ssvm.execution;

import dev.xdark.ssvm.value.TopValue;
import dev.xdark.ssvm.value.Value;

/**
 * Method execution stack.
 *
 * @author xDark
 */
public final class Stack {

	private final Value[] stack;
	private int cursor;

	/**
	 * @param maxSize
	 * 		The maximum size of the stack.
	 */
	public Stack(int maxSize) {
		stack = new Value[maxSize];
	}

	/**
	 * Pushes value onto the stack.
	 *
	 * @param value
	 * 		Value to push.
	 */
	public void push(Value value) {
		stack[cursor++] = value;
	}

	/**
	 * Pushes wide value onto the stack.
	 * Inserts TOP after.
	 *
	 * @param value
	 * 		Value to push.
	 */
	public void pushWide(Value value) {
		var stack = this.stack;
		int cursor = this.cursor;
		stack[cursor++] = value;
		stack[cursor++] = TopValue.INSTANCE;
		this.cursor = cursor;
	}

	/**
	 * Pushes generic value onto the stack.
	 * If the value is wide, TOP will also be pushed.
	 *
	 * @param value
	 * 		Value to push.
	 */
	public void pushGeneric(Value value) {
		if (value.isWide()) {
			pushWide(value);
		} else {
			push(value);
		}
	}

	/**
	 * Pops value off the stack
	 *
	 * @param <V>
	 * 		Type of the value.
	 *
	 * @return value popped off the stack.
	 */
	public <V extends Value> V pop() {
		return (V) stack[--cursor];
	}

	public <V extends Value> V popWide() {
		var stack = this.stack;
		int cursor = this.cursor;
		Value top = stack[--cursor];
		if (top != TopValue.INSTANCE) {
			throw new IllegalStateException("Expected to pop TOP value, but got: " + top);
		}
		return pop();
	}
}
