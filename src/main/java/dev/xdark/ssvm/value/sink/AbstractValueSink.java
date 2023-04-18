package dev.xdark.ssvm.value.sink;

import dev.xdark.ssvm.execution.PanicException;

public abstract class AbstractValueSink implements ValueSink {
	protected boolean set;

	public void reset() {
		set = false;
	}

	public final boolean isSet() {
		return set;
	}

	protected final void check() {
		if (set) {
			throw new PanicException("Expected one value to be sinked");
		}
		set = true;
	}

	protected static void expect(String expected, String actual) {
		throw new PanicException(expected + " was expected, but got " + actual);
	}
}
