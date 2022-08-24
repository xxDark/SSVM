package dev.xdark.ssvm.value.sink;

import dev.xdark.ssvm.value.ObjectValue;

public final class BlackholeValueSink extends AbstractValueSink {

	@Override
	public void acceptReference(ObjectValue value) {
		check();
	}

	@Override
	public void acceptLong(long value) {
		check();
	}

	@Override
	public void acceptDouble(double value) {
		check();
	}

	@Override
	public void acceptInt(int value) {
		check();
	}

	@Override
	public void acceptFloat(float value) {
		check();
	}
}
