package dev.xdark.ssvm.invoke;

import dev.xdark.ssvm.execution.Locals;
import org.objectweb.asm.Type;

/**
 * Int argument.
 *
 * @author xDark
 */
public final class IntArgument implements Argument {
	private final int value;

	IntArgument(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	@Override
	public int store(Locals locals, int index) {
		locals.setInt(index, value);
		return 1;
	}

	@Override
	public Type getType() {
		return Type.INT_TYPE;
	}
}
