package dev.xdark.ssvm;

import dev.xdark.ssvm.mirror.type.PrimitiveClass;
import dev.xdark.ssvm.symbol.Primitives;

/**
 * Implementation of VM primitives that
 * always throws exception due to uninitialized VM.
 *
 * @author xDark
 */
final class UninitializedPrimitives implements Primitives {

	@Override
	public PrimitiveClass longPrimitive() {
		return uninitialized();
	}

	@Override
	public PrimitiveClass doublePrimitive() {
		return uninitialized();
	}

	@Override
	public PrimitiveClass intPrimitive() {
		return uninitialized();
	}

	@Override
	public PrimitiveClass floatPrimitive() {
		return uninitialized();
	}

	@Override
	public PrimitiveClass charPrimitive() {
		return uninitialized();
	}

	@Override
	public PrimitiveClass shortPrimitive() {
		return uninitialized();
	}

	@Override
	public PrimitiveClass bytePrimitive() {
		return uninitialized();
	}

	@Override
	public PrimitiveClass booleanPrimitive() {
		return uninitialized();
	}

	@Override
	public PrimitiveClass voidPrimitive() {
		return uninitialized();
	}

	private static PrimitiveClass uninitialized() {
		throw new IllegalStateException("VM is not initialized!");
	}
}
