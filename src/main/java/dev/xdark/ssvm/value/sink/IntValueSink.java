package dev.xdark.ssvm.value.sink;

import dev.xdark.ssvm.value.ObjectValue;

public final class IntValueSink extends AbstractValueSink {
	private int value;

	@Override
	public void acceptReference(ObjectValue value) {
		expect("int", "reference");
	}

	@Override
	public void acceptLong(long value) {
		expect("int", "long");
	}

	@Override
	public void acceptDouble(double value) {
		expect("int", "double");
	}

	@Override
	public void acceptInt(int value) {
		this.value = value;
	}

	@Override
	public void acceptFloat(float value) {
		expect("int", "float");
	}

	@Override
	public void reset() {
		super.reset();
		value = 0;
	}

	public int getValue() {
		return value;
	}
}
