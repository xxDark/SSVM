package dev.xdark.ssvm.symbol;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.mirror.MirrorFactory;
import dev.xdark.ssvm.mirror.PrimitiveClass;
import org.objectweb.asm.Type;

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
		MirrorFactory factory = vm.getMirrorFactory();
		longPrimitive = factory.newPrimitiveClass("long", "J", Type.LONG);
		doublePrimitive = factory.newPrimitiveClass("double", "D", Type.DOUBLE);
		intPrimitive = factory.newPrimitiveClass("int", "I", Type.INT);
		floatPrimitive = factory.newPrimitiveClass("float", "F", Type.FLOAT);
		charPrimitive = factory.newPrimitiveClass("char", "C", Type.CHAR);
		shortPrimitive = factory.newPrimitiveClass("short", "S", Type.SHORT);
		bytePrimitive = factory.newPrimitiveClass("byte", "B", Type.BYTE);
		booleanPrimitive = factory.newPrimitiveClass("boolean", "Z", Type.BOOLEAN);
		voidPrimitive = factory.newPrimitiveClass("void", "V", Type.VOID);
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
