package dev.xdark.ssvm.value.sink;

import dev.xdark.ssvm.value.ObjectValue;

/**
 * Value sink that accepts any value.
 * Doubles/floats are encoded as longs/ints respectively.
 *
 * @author xDark
 */
public final class ReflectionSink extends AbstractValueSink {
	public long longValue;
	public int intValue;
	public ObjectValue referenceValue;

	@Override
	public void acceptReference(ObjectValue value) {
		check();
		referenceValue = value;
	}

	@Override
	public void acceptLong(long value) {
		check();
		longValue = value;
	}

	@Override
	public void acceptDouble(double value) {
		acceptLong(Double.doubleToRawLongBits(value));
	}

	@Override
	public void acceptInt(int value) {
		check();
		intValue = value;
	}

	@Override
	public void acceptFloat(float value) {
		acceptInt(Float.floatToRawIntBits(value));
	}
}
