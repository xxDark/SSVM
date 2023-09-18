package dev.xdark.ssvm.invoke;

import dev.xdark.ssvm.execution.Locals;
import org.objectweb.asm.Type;

/**
 * Int argument.
 *
 * @author xDark
 */
public final class DoubleArgument implements Argument {
	private final double value;

	DoubleArgument(double value) {
		this.value = value;
	}

	public double getValue() {
		return value;
	}

	@Override
	public int store(Locals locals, int index) {
		locals.setDouble(index, value);
		return 2;
	}

	@Override
	public Type getType() {
		return Type.DOUBLE_TYPE;
	}
}
