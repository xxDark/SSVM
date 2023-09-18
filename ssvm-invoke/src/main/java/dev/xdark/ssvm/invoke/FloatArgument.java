package dev.xdark.ssvm.invoke;

import dev.xdark.ssvm.execution.Locals;
import org.objectweb.asm.Type;

/**
 * Float argument.
 *
 * @author xDark
 */
public final class FloatArgument implements Argument {
	private final float value;

	FloatArgument(float value) {
		this.value = value;
	}

	public float getValue() {
		return value;
	}

	@Override
	public int store(Locals locals, int index) {
		locals.setFloat(index, value);
		return 1;
	}

	@Override
	public Type getType() {
		return Type.FLOAT_TYPE;
	}
}
