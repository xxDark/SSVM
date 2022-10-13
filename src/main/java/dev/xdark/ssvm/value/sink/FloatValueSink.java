package dev.xdark.ssvm.value.sink;

import dev.xdark.ssvm.value.ObjectValue;

/**
 * Value sink that accepts floats.
 *
 * @author xDark
 */
public final class FloatValueSink extends AbstractValueSink {
	private float value;

	@Override
	public void acceptReference(ObjectValue value) {
		expect("float", "reference");
	}

	@Override
	public void acceptLong(long value) {
		expect("float", "long");
	}

	@Override
	public void acceptDouble(double value) {
		expect("float", "double");
	}

	@Override
	public void acceptInt(int value) {
		expect("float", "int");
	}

	@Override
	public void acceptFloat(float value) {
		this.value = value;
	}

	@Override
	public void reset() {
		super.reset();
		value = 0.F;
	}

	public float getValue() {
		return value;
	}
}
