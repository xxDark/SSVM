package dev.xdark.ssvm.symbol;

import dev.xdark.ssvm.mirror.PrimitiveClass;

/**
 * Implementation of VM primitives that
 * always throws exception due to uninitialized VM.
 *
 * @author xDark
 */
public final class UninitializedVMPrimitives implements VMPrimitives {

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
