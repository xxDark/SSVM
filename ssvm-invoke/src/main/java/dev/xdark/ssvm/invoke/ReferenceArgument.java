package dev.xdark.ssvm.invoke;

import dev.xdark.ssvm.execution.Locals;
import dev.xdark.ssvm.value.ObjectValue;
import org.objectweb.asm.Type;

/**
 * Reference argument.
 *
 * @author xDark
 */
public final class ReferenceArgument implements Argument {
	private final ObjectValue value;

	ReferenceArgument(ObjectValue value) {
		this.value = value;
	}

	public ObjectValue getValue() {
		return value;
	}

	@Override
	public int store(Locals locals, int index) {
		locals.setReference(index, value);
		return 1;
	}

	@Override
	public Type getType() {
		return value.getJavaClass().getType();
	}
}
