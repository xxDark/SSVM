package dev.xdark.ssvm;

import dev.xdark.ssvm.mirror.type.PrimitiveClass;
import dev.xdark.ssvm.symbol.Primitives;

/**
 * State-dependent VM primitives.
 *
 * @author xDark
 */
final class DelegatingPrimitives implements Primitives {

	private Primitives primitives;

	@Override
	public PrimitiveClass longPrimitive() {
		return primitives.longPrimitive();
	}

	@Override
	public PrimitiveClass doublePrimitive() {
		return primitives.doublePrimitive();
	}

	@Override
	public PrimitiveClass intPrimitive() {
		return primitives.intPrimitive();
	}

	@Override
	public PrimitiveClass floatPrimitive() {
		return primitives.floatPrimitive();
	}

	@Override
	public PrimitiveClass charPrimitive() {
		return primitives.charPrimitive();
	}

	@Override
	public PrimitiveClass shortPrimitive() {
		return primitives.shortPrimitive();
	}

	@Override
	public PrimitiveClass bytePrimitive() {
		return primitives.bytePrimitive();
	}

	@Override
	public PrimitiveClass booleanPrimitive() {
		return primitives.booleanPrimitive();
	}

	@Override
	public PrimitiveClass voidPrimitive() {
		return primitives.voidPrimitive();
	}

	/**
	 * @param primitives New primitives.
	 */
	void setPrimitives(Primitives primitives) {
		this.primitives = primitives;
	}
}
