package dev.xdark.ssvm.invoke;

import dev.xdark.ssvm.execution.Locals;

/**
 * Long argument.
 *
 * @author xDark
 */
final class LongArgument implements Argument {
	private final long value;

	LongArgument(long value) {
		this.value = value;
	}

	@Override
	public int store(Locals locals, int index) {
		locals.setLong(index, value);
		return 2;
	}
}
