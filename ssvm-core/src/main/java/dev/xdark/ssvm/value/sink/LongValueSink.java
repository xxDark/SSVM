package dev.xdark.ssvm.value.sink;

import dev.xdark.ssvm.value.ObjectValue;

/**
 * Value sink that accepts longs.
 *
 * @author xDark
 */
public final class LongValueSink extends AbstractValueSink {
	private long value;

	@Override
	public void acceptReference(ObjectValue value) {
		expect("long", "reference");
	}

	@Override
	public void acceptLong(long value) {
		check();
		this.value = value;
	}

	@Override
	public void acceptDouble(double value) {
		expect("long", "double");
	}

	@Override
	public void acceptInt(int value) {
		expect("long", "int");
	}

	@Override
	public void acceptFloat(float value) {
		expect("long", "float");
	}

	@Override
	public void reset() {
		super.reset();
		value = 0L;
	}

	public long getValue() {
		return value;
	}
}
