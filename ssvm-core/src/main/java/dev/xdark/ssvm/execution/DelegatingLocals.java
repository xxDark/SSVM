package dev.xdark.ssvm.execution;

import dev.xdark.ssvm.memory.allocation.MemoryData;
import dev.xdark.ssvm.thread.ThreadMemoryData;
import dev.xdark.ssvm.value.ObjectValue;

public class DelegatingLocals implements LocalsWithMemory {
	private final Locals delegate;

	public DelegatingLocals(Locals delegate) {
		this.delegate = delegate;
	}

	@Override
	public void setReference(int index, ObjectValue value) {
		delegate.setReference(index, value);
	}

	@Override
	public void setLong(int index, long value) {
		delegate.setLong(index, value);
	}

	@Override
	public void setDouble(int index, double value) {
		delegate.setDouble(index, value);
	}

	@Override
	public void setInt(int index, int value) {
		delegate.setInt(index, value);
	}

	@Override
	public void setFloat(int index, float value) {
		delegate.setFloat(index, value);
	}

	@Override
	public <V extends ObjectValue> V loadReference(int index) {
		return delegate.loadReference(index);
	}

	@Override
	public long loadLong(int index) {
		return delegate.loadLong(index);
	}

	@Override
	public double loadDouble(int index) {
		return delegate.loadDouble(index);
	}

	@Override
	public int loadInt(int index) {
		return delegate.loadInt(index);
	}

	@Override
	public float loadFloat(int index) {
		return delegate.loadFloat(index);
	}

	@Override
	public void copyFrom(Locals locals, int srcOffset, int destOffset, int length) {
		delegate.copyFrom(locals, srcOffset, destOffset, length);
	}

	@Override
	public int maxSlots() {
		return delegate.maxSlots();
	}

	@Override
	public MemoryData region() {
		if (delegate instanceof LocalsWithMemory)
			return ((LocalsWithMemory) delegate).region();
		throw new UnsupportedOperationException("Delegate type does not support region()");
	}

	@Override
	public void reset(ThreadMemoryData data) {
		if (delegate instanceof LocalsWithMemory)
			((LocalsWithMemory) delegate).reset(data);
	}
}
