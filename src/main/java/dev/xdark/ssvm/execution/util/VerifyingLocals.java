package dev.xdark.ssvm.execution.util;

import dev.xdark.ssvm.execution.Locals;
import dev.xdark.ssvm.value.Value;

import java.util.Objects;

/**
 * Locals that verify set operations.
 *
 * @author xDark
 */
public final class VerifyingLocals implements Locals {
	private final Locals delegate;

	/**
	 * @param delegate Backing locals.
	 */
	public VerifyingLocals(Locals delegate) {
		this.delegate = delegate;
	}

	@Override
	public void set(int index, Value value) {
		delegate.set(index, Objects.requireNonNull(value, "value"));
	}

	@Override
	public void setWide(int index, Value value) {
		if (!Objects.requireNonNull(value, "value").isWide()) {
			throw new IllegalStateException("Must use set instead");
		}
		delegate.setWide(index, value);
	}

	@Override
	public <V extends Value> V load(int index) {
		return delegate.load(index);
	}

	@Override
	public void copyFrom(Locals locals, int srcOffset, int destOffset, int length) {
		delegate.copyFrom(locals, srcOffset, destOffset, length);
	}

	@Override
	public void copyFrom(Value[] locals, int srcOffset, int destOffset, int length) {
		delegate.copyFrom(locals, srcOffset, destOffset, length);
	}

	@Override
	public Value[] getTable() {
		return delegate.getTable();
	}

	@Override
	public int maxSlots() {
		return delegate.maxSlots();
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof Locals && delegate.equals(obj);
	}

	@Override
	public int hashCode() {
		return delegate.hashCode();
	}
}
