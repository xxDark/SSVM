package dev.xdark.ssvm.value.sink;

import dev.xdark.ssvm.value.ObjectValue;

/**
 * Value sink.
 *
 * @author xDark
 */
public interface ValueSink {

	/**
	 * @param value Value to accept.
	 */
	void acceptReference(ObjectValue value);

	/**
	 * @param value Value to accept.
	 */
	void acceptLong(long value);

	/**
	 * @param value Value to accept.
	 */
	void acceptDouble(double value);

	/**
	 * @param value Value to accept.
	 */
	void acceptInt(int value);

	/**
	 * @param value Value to accept.
	 */
	void acceptFloat(float value);
}
