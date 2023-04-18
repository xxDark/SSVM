package dev.xdark.ssvm.value.sink;

import dev.xdark.ssvm.value.ObjectValue;

public interface ValueSink {

	void acceptReference(ObjectValue value);

	void acceptLong(long value);

	void acceptDouble(double value);

	void acceptInt(int value);

	void acceptFloat(float value);
}
