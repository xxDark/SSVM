package dev.xdark.ssvm.invoke;

import dev.xdark.ssvm.execution.Locals;
import dev.xdark.ssvm.value.ObjectValue;
import org.objectweb.asm.Type;

/**
 * Method argument.
 *
 * @author xDark
 */
public interface Argument {

	/**
	 * Stores argument.
	 *
	 * @param locals Locals to store argument to.
	 * @param index  Argument index.
	 * @return The amount of slots occupied by this argument.
	 */
	int store(Locals locals, int index);

	/**
	 * @return Argument value type.
	 */
	Type getType();

	/**
	 * {@literal long} argument.
	 *
	 * @param value Argument value.
	 * @return Argument wrapper.
	 */
	static Argument int64(long value) {
		return new LongArgument(value);
	}

	/**
	 * {@literal double} argument.
	 *
	 * @param value Argument value.
	 * @return Argument wrapper.
	 */
	static Argument float64(double value) {
		return new DoubleArgument(value);
	}

	/**
	 * {@literal int} argument.
	 *
	 * @param value Argument value.
	 * @return Argument wrapper.
	 */
	static Argument int32(int value) {
		return new IntArgument(value);
	}

	/**
	 * {@literal float} argument.
	 *
	 * @param value Argument value.
	 * @return Argument wrapper.
	 */
	static Argument float32(float value) {
		return new FloatArgument(value);
	}

	/**
	 * {@literal reference} argument.
	 *
	 * @param value Argument value.
	 * @return Argument wrapper.
	 */
	static Argument reference(ObjectValue value) {
		return new ReferenceArgument(value);
	}
}
