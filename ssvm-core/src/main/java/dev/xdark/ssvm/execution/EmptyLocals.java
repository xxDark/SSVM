package dev.xdark.ssvm.execution;

import dev.xdark.ssvm.value.ObjectValue;

/**
 * Empty locals.
 *
 * @author xDark
 */
public final class EmptyLocals implements Locals {
	public static final EmptyLocals INSTANCE = new EmptyLocals();

	private EmptyLocals() {
	}

	@Override
	public void setReference(int index, ObjectValue value) {
		panicEmpty();
	}

	@Override
	public void setLong(int index, long value) {
		panicEmpty();
	}

	@Override
	public void setDouble(int index, double value) {
		panicEmpty();
	}

	@Override
	public void setInt(int index, int value) {
		panicEmpty();
	}

	@Override
	public void setFloat(int index, float value) {
		panicEmpty();
	}

	@Override
	public <V extends ObjectValue> V loadReference(int index) {
		return panicEmpty();
	}

	@Override
	public long loadLong(int index) {
		return panicEmpty();
	}

	@Override
	public double loadDouble(int index) {
		return panicEmpty();
	}

	@Override
	public int loadInt(int index) {
		return panicEmpty();
	}

	@Override
	public float loadFloat(int index) {
		return panicEmpty();
	}

	@Override
	public void copyFrom(Locals locals, int srcOffset, int destOffset, int length) {
		if (length != 0) {
			panicEmpty();
		}
	}

	@Override
	public int maxSlots() {
		return panicEmpty();
	}

	private static <T> T panicEmpty() {
		throw new PanicException("The stack size is 0");
	}
}
