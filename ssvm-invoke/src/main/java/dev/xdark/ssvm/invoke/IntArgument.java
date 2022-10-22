package dev.xdark.ssvm.invoke;

import dev.xdark.ssvm.execution.Locals;

/**
 * Int argument.
 *
 * @author xDark
 */
final class IntArgument implements Argument {
	private final int value;

	IntArgument(int value) {
		this.value = value;
	}

	@Override
	public int store(Locals locals, int index) {
		locals.setInt(index, value);
		return 1;
	}
}
