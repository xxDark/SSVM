package dev.xdark.ssvm.execution;

import dev.xdark.ssvm.value.ObjectValue;
import dev.xdark.ssvm.value.Value;

/**
 * Empty stack.
 *
 * @author xDark
 */
public final class EmptyStack implements Stack {
	public static final EmptyStack INSTANCE = new EmptyStack();

	private EmptyStack() {
	}

	@Override
	public void pushReference(ObjectValue value) {
		panicEmpty();
	}

	@Override
	public void pushLong(long value) {
		panicEmpty();
	}

	@Override
	public void pushDouble(double value) {
		panicEmpty();
	}

	@Override
	public void pushInt(int value) {
		panicEmpty();
	}

	@Override
	public void pushFloat(float value) {
		panicEmpty();
	}

	@Override
	public void pushGeneric(Value value) {
		panicEmpty();
	}

	@Override
	public void pop() {
		panicEmpty();
	}

	@Override
	public <V extends ObjectValue> V popReference() {
		return panicEmpty();
	}

	@Override
	public long popLong() {
		return panicEmpty();
	}

	@Override
	public double popDouble() {
		return panicEmpty();
	}

	@Override
	public int popInt() {
		return panicEmpty();
	}

	@Override
	public float popFloat() {
		return panicEmpty();
	}

	@Override
	public char popChar() {
		return panicEmpty();
	}

	@Override
	public short popShort() {
		return panicEmpty();
	}

	@Override
	public byte popByte() {
		return panicEmpty();
	}

	@Override
	public <V extends ObjectValue> V peekReference() {
		return panicEmpty();
	}

	@Override
	public long peekLong() {
		return panicEmpty();
	}

	@Override
	public double peekDouble() {
		return panicEmpty();
	}

	@Override
	public int peekInt() {
		return panicEmpty();
	}

	@Override
	public float peekFloat() {
		return panicEmpty();
	}

	@Override
	public char peekChar() {
		return panicEmpty();
	}

	@Override
	public short peekShort() {
		return panicEmpty();
	}

	@Override
	public byte peekByte() {
		return panicEmpty();
	}

	@Override
	public void dup() {
		panicEmpty();
	}

	@Override
	public void dupx1() {
		panicEmpty();
	}

	@Override
	public void dupx2() {
		panicEmpty();
	}

	@Override
	public void dup2() {
		panicEmpty();
	}

	@Override
	public void dup2x1() {
		panicEmpty();
	}

	@Override
	public void dup2x2() {
		panicEmpty();
	}

	@Override
	public void swap() {
		panicEmpty();
	}

	@Override
	public void clear() {
	}

	@Override
	public int position() {
		return 0;
	}

	@Override
	public <V extends ObjectValue> V getReferenceAt(int index) {
		return panicEmpty();
	}

	@Override
	public long getLongAt(int index) {
		return panicEmpty();
	}

	@Override
	public double getDoubleAt(int index) {
		return panicEmpty();
	}

	@Override
	public int getIntAt(int index) {
		return panicEmpty();
	}

	@Override
	public float getFloatAt(int index) {
		return panicEmpty();
	}

	@Override
	public char getCharAt(int index) {
		return panicEmpty();
	}

	@Override
	public short getShortAt(int index) {
		return panicEmpty();
	}

	@Override
	public byte getByteAt(int index) {
		return panicEmpty();
	}

	@Override
	public void sinkInto(Locals locals, int dst, int count) {
		if (count != 0) {
			panicEmpty();
		}
	}

	@Override
	public void sinkInto(Locals locals, int count) {
		if (count != 0) {
			panicEmpty();
		}
	}

	@Override
	public void acceptReference(ObjectValue value) {
		panicEmpty();
	}

	@Override
	public void acceptLong(long value) {
		panicEmpty();
	}

	@Override
	public void acceptDouble(double value) {
		panicEmpty();
	}

	@Override
	public void acceptInt(int value) {
		panicEmpty();
	}

	@Override
	public void acceptFloat(float value) {
		panicEmpty();
	}

	private static <T> T panicEmpty() {
		throw new PanicException("The stack size is 0");
	}
}
