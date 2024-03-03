package dev.xdark.ssvm.execution;

import dev.xdark.ssvm.value.ObjectValue;
import dev.xdark.ssvm.value.Value;

public class DelegatingStack implements Stack {
	private final Stack delegate;

	public DelegatingStack(Stack delegate) {
		this.delegate = delegate;
	}

	@Override
	public void pushReference(ObjectValue value) {
		delegate.pushReference(value);
	}

	@Override
	public void pushLong(long value) {
		delegate.pushLong(value);
	}

	@Override
	public void pushDouble(double value) {
		delegate.pushDouble(value);
	}

	@Override
	public void pushInt(int value) {
		delegate.pushInt(value);
	}

	@Override
	public void pushFloat(float value) {
		delegate.pushFloat(value);
	}

	@Override
	public void pushGeneric(Value value) {
		delegate.pushGeneric(value);
	}

	@Override
	public void pop() {
		delegate.pop();
	}

	@Override
	public <V extends ObjectValue> V popReference() {
		return delegate.popReference();
	}

	@Override
	public long popLong() {
		return delegate.popLong();
	}

	@Override
	public double popDouble() {
		return delegate.popDouble();
	}

	@Override
	public int popInt() {
		return delegate.popInt();
	}

	@Override
	public float popFloat() {
		return delegate.popFloat();
	}

	@Override
	public char popChar() {
		return delegate.popChar();
	}

	@Override
	public short popShort() {
		return delegate.popShort();
	}

	@Override
	public byte popByte() {
		return delegate.popByte();
	}

	@Override
	public <V extends ObjectValue> V peekReference() {
		return delegate.peekReference();
	}

	@Override
	public long peekLong() {
		return delegate.peekLong();
	}

	@Override
	public double peekDouble() {
		return delegate.peekDouble();
	}

	@Override
	public int peekInt() {
		return delegate.peekInt();
	}

	@Override
	public float peekFloat() {
		return delegate.peekFloat();
	}

	@Override
	public char peekChar() {
		return delegate.peekChar();
	}

	@Override
	public short peekShort() {
		return delegate.peekShort();
	}

	@Override
	public byte peekByte() {
		return delegate.peekByte();
	}

	@Override
	public void dup() {
		delegate.dup();
	}

	@Override
	public void dupx1() {
		delegate.dupx1();
	}

	@Override
	public void dupx2() {
		delegate.dupx2();
	}

	@Override
	public void dup2() {
		delegate.dup2();
	}

	@Override
	public void dup2x1() {
		delegate.dup2x1();
	}

	@Override
	public void dup2x2() {
		delegate.dup2x2();
	}

	@Override
	public void swap() {
		delegate.swap();
	}

	@Override
	public void clear() {
		delegate.clear();
	}

	@Override
	public int position() {
		return delegate.position();
	}

	@Override
	public <V extends ObjectValue> V getReferenceAt(int index) {
		return delegate.getReferenceAt(index);
	}

	@Override
	public long getLongAt(int index) {
		return delegate.getLongAt(index);
	}

	@Override
	public double getDoubleAt(int index) {
		return delegate.getDoubleAt(index);
	}

	@Override
	public int getIntAt(int index) {
		return delegate.getIntAt(index);
	}

	@Override
	public float getFloatAt(int index) {
		return delegate.getFloatAt(index);
	}

	@Override
	public char getCharAt(int index) {
		return delegate.getCharAt(index);
	}

	@Override
	public short getShortAt(int index) {
		return delegate.getShortAt(index);
	}

	@Override
	public byte getByteAt(int index) {
		return delegate.getByteAt(index);
	}

	@Override
	public void sinkInto(Locals locals, int dst, int count) {
		delegate.sinkInto(locals, dst, count);
	}

	@Override
	public void sinkInto(Locals locals, int count) {
		delegate.sinkInto(locals, count);
	}

	@Override
	public void acceptReference(ObjectValue value) {
		delegate.acceptReference(value);
	}

	@Override
	public void acceptLong(long value) {
		delegate.acceptLong(value);
	}

	@Override
	public void acceptDouble(double value) {
		delegate.acceptDouble(value);
	}

	@Override
	public void acceptInt(int value) {
		delegate.acceptInt(value);
	}

	@Override
	public void acceptFloat(float value) {
		delegate.acceptFloat(value);
	}
}
