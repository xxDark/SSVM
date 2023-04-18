package dev.xdark.ssvm.invoke;

import dev.xdark.ssvm.execution.Locals;
import dev.xdark.ssvm.value.ObjectValue;

/**
 * Reference argument.
 *
 * @author xDark
 */
final class ReferenceArgument implements Argument {
	private final ObjectValue value;

	ReferenceArgument(ObjectValue value) {
		this.value = value;
	}

	@Override
	public int store(Locals locals, int index) {
		locals.setReference(index, value);
		return 1;
	}
}
