package dev.xdark.ssvm.util;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.mirror.PrimitiveClass;

/**
 * Declares VM primitive classes.
 *
 * @author xDark
 */
public final class VMPrimitives {

	public final PrimitiveClass longPrimitive;
	public final PrimitiveClass doublePrimitive;
	public final PrimitiveClass intPrimitive;
	public final PrimitiveClass floatPrimitive;
	public final PrimitiveClass charPrimitive;
	public final PrimitiveClass shortPrimitive;
	public final PrimitiveClass bytePrimitive;
	public final PrimitiveClass booleanPrimitive;
	public final PrimitiveClass voidPrimitive;

	/**
	 * @param vm
	 * 		VM instance.
	 */
	public VMPrimitives(VirtualMachine vm) {
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
}
