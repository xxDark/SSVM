package dev.xdark.ssvm.invoke;

import dev.xdark.ssvm.execution.Locals;

/**
 * Float argument.
 *
 * @author xDark
 */
final class FloatArgument implements Argument {
	private final float value;

	FloatArgument(float value) {
		this.value = value;
	}

	@Override
	public int store(Locals locals, int index) {
		locals.setFloat(index, value);
		return 1;
	}
}
