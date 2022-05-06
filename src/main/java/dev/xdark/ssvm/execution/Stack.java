package dev.xdark.ssvm.execution;

import dev.xdark.ssvm.value.Value;

import java.util.List;

/**
 * Method execution stack.
 *
 * @author xDark
 */
public interface Stack {

	/**
	 * Pushes value onto the stack.
	 *
	 * @param value
	 * 		Value to push.
	 */
	void push(Value value);

	/**
	 * Pushes wide value onto the stack.
	 * Inserts TOP after.
	 *
	 * @param value
	 * 		Value to push.
	 */
	void pushWide(Value value);

	/**
	 * Pushes generic value onto the stack.
	 * If the value is wide, TOP will also be pushed.
	 *
	 * @param value
	 * 		Value to push.
	 */
	void pushGeneric(Value value);

	/**
	 * Pushes value onto the stack.
	 * Does not check whether the value
	 * is wide or not.
	 *
	 * @param value
	 * 		Value to push.
	 */
	void pushRaw(Value value);

	/**
	 * Pops value off the stack.
	 *
	 * @param <V>
	 * 		Type of the value.
	 *
	 * @return value popped off the stack.
	 */
	<V extends Value> V pop();

	/**
	 * Pops wide value off the stack.
	 *
	 * @param <V>
	 * 		Type of the value.
	 *
	 * @return wide value popped off the stack.
	 *
	 * @throws IllegalStateException
	 * 		If wide value does not occupy two slots.
	 */
	<V extends Value> V popWide();

	/**
	 * Pops generic value off the stack.
	 *
	 * @param <V>
	 * 		Type of the value.
	 *
	 * @return generic value popped off the stack.
	 */
	<V extends Value> V popGeneric();

	/**
	 * Peeks value from the stack.
	 *
	 * @param <V>
	 * 		Type of the value.
	 *
	 * @return value peeked from the stack.
	 */
	<V extends Value> V peek();

	/**
	 * Polls value from the stack.
	 *
	 * @param <V>
	 * 		Value tpye.
	 *
	 * @return tail value of the stack or {@code null},
	 * if stack is empty.
	 */
	<V extends Value> V poll();

	/**
	 * Duplicates value on the stack.
	 */
	default void dup() {
		push(peek());
	}

	/**
	 * Duplicate the top operand stack value and insert two values down.
	 */
	default void dupx1() {
		Value v1 = pop();
		Value v2 = pop();
		push(v1);
		push(v2);
		push(v1);
	}

	/**
	 * Duplicate the top operand stack value
	 * and insert two or three values down.
	 */
	default void dupx2() {
		Value v1 = pop();
		Value v2 = pop();
		Value v3 = pop();
		pushRaw(v1);
		pushRaw(v3);
		pushRaw(v2);
		pushRaw(v1);
	}

	/**
	 * Duplicate the top one or two operand stack values.
	 */
	default void dup2() {
		Value v1 = pop();
		Value v2 = pop();
		pushRaw(v2);
		pushRaw(v1);
		pushRaw(v2);
		pushRaw(v1);
	}

	/**
	 * Duplicate the top one or two operand stack values
	 * and insert two or three values down.
	 */
	default void dup2x1() {
		Value v1 = pop();
		Value v2 = pop();
		Value v3 = pop();
		pushRaw(v2);
		pushRaw(v1);
		pushRaw(v3);
		pushRaw(v2);
		pushRaw(v1);
	}

	/**
	 * Duplicate the top one or two operand stack values
	 * and insert two, three, or four values down.
	 */
	default void dup2x2() {
		Value v1 = pop();
		Value v2 = pop();
		Value v3 = pop();
		Value v4 = pop();
		pushRaw(v2);
		pushRaw(v1);
		pushRaw(v4);
		pushRaw(v3);
		pushRaw(v2);
		pushRaw(v1);
	}

	/**
	 * Swap the top two operand stack values.
	 */
	default void swap() {
		Value v1 = pop();
		Value v2 = pop();
		push(v1);
		push(v2);
	}

	/**
	 * Returns whether the stack is empty.
	 *
	 * @return {@code true} if stack is empty,
	 * {@code false} otherwise.
	 */
	default boolean isEmpty() {
		return position() == 0;
	}

	/**
	 * Resets the stack.
	 */
	void clear();

	/**
	 * @return current cursor position.
	 */
	int position();

	/**
	 * Gets value on the stack by an index.
	 *
	 * @param index
	 * 		Value position.
	 *
	 * @return value at the specific position.
	 */
	Value getAt(int index);

	/**
	 * @return stack content as a list view.
	 */
	List<Value> view();
}
