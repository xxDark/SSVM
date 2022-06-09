package dev.xdark.ssvm.symbol;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.mirror.PrimitiveClass;

/**
 * Implementation of initialized VM primitives.
 *
 * @author xDark
 */
public final class InitializedVMPrimitives implements VMPrimitives {

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
	 * @param vm VM instance.
	 */
	public InitializedVMPrimitives(VirtualMachine vm) {
		vm.assertInitialized();
		longPrimitive = new PrimitiveClass(vm, "long", "J");
		doublePrimitive = new PrimitiveClass(vm, "double", "D");
		intPrimitive = new PrimitiveClass(vm, "int", "I");
		floatPrimitive = new PrimitiveClass(vm, "float", "F");
		charPrimitive = new PrimitiveClass(vm, "char", "C");
		shortPrimitive = new PrimitiveClass(vm, "short", "S");
		bytePrimitive = new PrimitiveClass(vm, "byte", "B");
		booleanPrimitive = new PrimitiveClass(vm, "boolean", "Z");
		voidPrimitive = new PrimitiveClass(vm, "void", "V");
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
