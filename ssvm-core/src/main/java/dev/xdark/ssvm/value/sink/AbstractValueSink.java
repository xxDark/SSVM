package dev.xdark.ssvm.value.sink;

import dev.xdark.ssvm.execution.PanicException;

public abstract class AbstractValueSink implements ValueSink {
	protected boolean set;

	/**
	 * Resets this sink.
	 */
	public void reset() {
		set = false;
	}

	/**
	 * @return {@code true} if the value is set.
	 */
	public final boolean isSet() {
		return set;
	}

	/**
	 * Checks and sets the flag whether the value
	 * has been accepted or not.
	 */
	protected final void check() {
		if (set) {
			throw new PanicException("Expected one value to be accepted");
		}
		set = true;
	}

	protected static void expect(String expected, String actual) {
		throw new PanicException(expected + " was expected, but got " + actual);
	}
}
