package dev.xdark.ssvm.value.sink;

import dev.xdark.ssvm.value.ObjectValue;

public final class ReferenceValueSink extends AbstractValueSink {
	private ObjectValue value;

	@Override
	public void acceptReference(ObjectValue value) {
		check();
		this.value = value;
	}

	@Override
	public void acceptLong(long value) {
		expect("reference", "long");
	}

	@Override
	public void acceptDouble(double value) {
		expect("reference", "double");
	}

	@Override
	public void acceptInt(int value) {
		expect("reference", "int");
	}

	@Override
	public void acceptFloat(float value) {
		expect("reference", "float");
	}

	@Override
	public void reset() {
		super.reset();
		value = null;
	}

	public ObjectValue getValue() {
		return value;
	}
}
