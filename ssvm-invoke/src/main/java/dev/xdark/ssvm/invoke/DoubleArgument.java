package dev.xdark.ssvm.invoke;

import dev.xdark.ssvm.execution.Locals;

/**
 * Int argument.
 *
 * @author xDark
 */
final class DoubleArgument implements Argument {
	private final double value;

	DoubleArgument(double value) {
		this.value = value;
	}

	@Override
	public int store(Locals locals, int index) {
		locals.setDouble(index, value);
		return 2;
	}
}
