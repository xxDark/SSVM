package dev.xdark.ssvm.value.sink;

import dev.xdark.ssvm.value.ObjectValue;

public final class DoubleValueSink extends AbstractValueSink {
	private double value;

	@Override
	public void acceptReference(ObjectValue value) {
		expect("double", "reference");
	}

	@Override
	public void acceptLong(long value) {
		expect("double", "long");
	}

	@Override
	public void acceptDouble(double value) {
		this.value = value;
	}

	@Override
	public void acceptInt(int value) {
		expect("double", "int");
	}

	@Override
	public void acceptFloat(float value) {
		expect("double", "float");
	}

	@Override
	public void reset() {
		super.reset();
		value = 0.;
	}

	public double getValue() {
		return value;
	}
}
