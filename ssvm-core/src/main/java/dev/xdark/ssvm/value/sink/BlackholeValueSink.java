package dev.xdark.ssvm.value.sink;

import dev.xdark.ssvm.value.ObjectValue;

/**
 * Value sink that discards any value.
 *
 * @author xDark
 */
public final class BlackholeValueSink extends AbstractValueSink {
	public static final ValueSink INSTANCE = new BlackholeValueSink();

	@Override
	public void acceptReference(ObjectValue value) {
	}

	@Override
	public void acceptLong(long value) {
	}

	@Override
	public void acceptDouble(double value) {
	}

	@Override
	public void acceptInt(int value) {
	}

	@Override
	public void acceptFloat(float value) {
	}
}
