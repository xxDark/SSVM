package dev.xdark.ssvm.execution;

import dev.xdark.ssvm.value.ObjectValue;
import dev.xdark.ssvm.value.Value;

/**
 * Method execution stack.
 *
 * @author xDark
 */
public interface Stack {

	/**
	 * @param value Value to push.
	 */
	void pushReference(ObjectValue value);

	/**
	 * @param value Value to push.
	 */
	void pushLong(long value);

	/**
	 * @param value Value to push.
	 */
	void pushDouble(double value);

	/**
	 * @param value Value to push.
	 */
	void pushInt(int value);

	/**
	 * @param value Value to push.
	 */
	void pushFloat(float value);

	/**
	 * Pushes generic value onto the stack.
	 * If the value is wide, TOP will also be pushed.
	 *
	 * @param value Value to push.
	 */
	void pushGeneric(Value value);

	/**
	 * Pops value off the stack.
	 */
	void pop();

	/**
	 * Pops reference off the stack.
	 *
	 * @param <V> Type of the value.
	 * @return value popped off the stack.
	 */
	<V extends ObjectValue> V popReference();

	/**
	 * Pops long off the stack.
	 *
	 * @return long value.
	 */
	long popLong();

	/**
	 * Pops double off the stack.
	 *
	 * @return double value.
	 */
	double popDouble();

	/**
	 * Pops int off the stack.
	 *
	 * @return int value.
	 */
	int popInt();

	/**
	 * Pops float off the stack.
	 *
	 * @return float value.
	 */
	float popFloat();

	/**
	 * Pops char off the stack.
	 *
	 * @return char value.
	 */
	char popChar();

	/**
	 * Pops short off the stack.
	 *
	 * @return short value.
	 */
	short popShort();

	/**
	 * Pops byte off the stack.
	 *
	 * @return int value.
	 */
	byte popByte();

	/**
	 * @param <V> Type of the value.
	 * @return refrerence value.
	 */
	<V extends ObjectValue> V peekReference();

	/**
	 * @return long value.
	 */
	long peekLong();

	/**
	 * @return double value.
	 */
	double peekDouble();

	/**
	 * @return int value.
	 */
	int peekInt();

	/**
	 * @return float value.
	 */
	float peekFloat();

	/**
	 * @return char value.
	 */
	char peekChar();

	/**
	 * @return short value.
	 */
	short peekShort();

	/**
	 * @return byte value.
	 */
	byte peekByte();

	/**
	 * Duplicates value on the stack.
	 */
	void dup();

	/**
	 * Duplicate the top operand stack value and insert two values down.
	 */
	void dupx1();

	/**
	 * Duplicate the top operand stack value
	 * and insert two or three values down.
	 */
	void dupx2();

	/**
	 * Duplicate the top one or two operand stack values.
	 */
	void dup2();

	/**
	 * Duplicate the top one or two operand stack values
	 * and insert two or three values down.
	 */
	void dup2x1();

	/**
	 * Duplicate the top one or two operand stack values
	 * and insert two, three, or four values down.
	 */
	void dup2x2();

	/**
	 * Swap the top two operand stack values.
	 */
	void swap();

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
	 * @param index Value position.
	 * @return value at the specific position.
	 */
	<V extends ObjectValue> V getReferenceAt(int index);

	/**
	 * Gets value on the stack by an index.
	 *
	 * @param index Value position.
	 * @return value at the specific position.
	 */
	long getLongAt(int index);

	/**
	 * Gets value on the stack by an index.
	 *
	 * @param index Value position.
	 * @return value at the specific position.
	 */
	double getDoubleAt(int index);

	/**
	 * Gets value on the stack by an index.
	 *
	 * @param index Value position.
	 * @return value at the specific position.
	 */
	int getIntAt(int index);

	/**
	 * Gets value on the stack by an index.
	 *
	 * @param index Value position.
	 * @return value at the specific position.
	 */
	float getFloatAt(int index);

	/**
	 * Gets value on the stack by an index.
	 *
	 * @param index Value position.
	 * @return value at the specific position.
	 */
	char getCharAt(int index);

	/**
	 * Gets value on the stack by an index.
	 *
	 * @param index Value position.
	 * @return value at the specific position.
	 */
	short getShortAt(int index);

	/**
	 * Gets value on the stack by an index.
	 *
	 * @param index Value position.
	 * @return value at the specific position.
	 */
	byte getByteAt(int index);

	/**
	 * Copies stack contents into the locals.
	 * Decreases current position.
	 *
	 * @param locals Locals to copy into.
	 * @param dst    Copy offset.
	 * @param count  Locals count.
	 */
	void sinkInto(Locals locals, int dst, int count);

	/**
	 * Copies stack contents into the locals.
	 * Decreases current position.
	 *
	 * @param locals Locals to copy into.
	 * @param count  Locals count.
	 */
	void sinkInto(Locals locals, int count);
}
