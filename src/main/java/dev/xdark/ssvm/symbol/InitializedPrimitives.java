package dev.xdark.ssvm.symbol;

import dev.xdark.ssvm.mirror.MirrorFactory;
import dev.xdark.ssvm.mirror.type.PrimitiveClass;
import org.objectweb.asm.Type;

/**
 * Implementation of initialized VM primitives.
 *
 * @author xDark
 */
public final class InitializedPrimitives implements Primitives {

	private final PrimitiveClass longPrimitive;
	private final PrimitiveClass doublePrimitive;
	private final PrimitiveClass intPrimitive;
	private final PrimitiveClass floatPrimitive;
	private final PrimitiveClass charPrimitive;
	private final PrimitiveClass shortPrimitive;
	private final PrimitiveClass bytePrimitive;
	private final PrimitiveClass booleanPrimitive;
	private final PrimitiveClass voidPrimitive;

	/**
	 * @param factory Mirror factory.
	 */
	public InitializedPrimitives(MirrorFactory factory) {
		longPrimitive = factory.newPrimitiveClass(Type.LONG_TYPE);
		doublePrimitive = factory.newPrimitiveClass(Type.DOUBLE_TYPE);
		intPrimitive = factory.newPrimitiveClass(Type.INT_TYPE);
		floatPrimitive = factory.newPrimitiveClass(Type.FLOAT_TYPE);
		charPrimitive = factory.newPrimitiveClass(Type.CHAR_TYPE);
		shortPrimitive = factory.newPrimitiveClass(Type.SHORT_TYPE);
		bytePrimitive = factory.newPrimitiveClass(Type.BYTE_TYPE);
		booleanPrimitive = factory.newPrimitiveClass(Type.BOOLEAN_TYPE);
		voidPrimitive = factory.newPrimitiveClass(Type.VOID_TYPE);
	}

	@Override
	public PrimitiveClass longPrimitive() {
		return longPrimitive;
	}

	@Override
	public PrimitiveClass doublePrimitive() {
		return doublePrimitive;
	}

	@Override
	public PrimitiveClass intPrimitive() {
		return intPrimitive;
	}

	@Override
	public PrimitiveClass floatPrimitive() {
		return floatPrimitive;
	}

	@Override
	public PrimitiveClass charPrimitive() {
		return charPrimitive;
	}

	@Override
	public PrimitiveClass shortPrimitive() {
		return shortPrimitive;
	}

	@Override
	public PrimitiveClass bytePrimitive() {
		return bytePrimitive;
	}

	@Override
	public PrimitiveClass booleanPrimitive() {
		return booleanPrimitive;
	}

	@Override
	public PrimitiveClass voidPrimitive() {
		return voidPrimitive;
	}
}
