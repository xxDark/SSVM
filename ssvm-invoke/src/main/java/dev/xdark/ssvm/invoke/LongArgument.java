package dev.xdark.ssvm.invoke;

import dev.xdark.ssvm.execution.Locals;
import org.objectweb.asm.Type;

/**
 * Long argument.
 *
 * @author xDark
 */
public final class LongArgument implements Argument {
	private final long value;

	LongArgument(long value) {
		this.value = value;
	}

	public long getValue() {
		return value;
	}

	@Override
	public int store(Locals locals, int index) {
		locals.setLong(index, value);
		return 2;
	}

	@Override
	public Type getType() {
		return Type.LONG_TYPE;
	}
}
