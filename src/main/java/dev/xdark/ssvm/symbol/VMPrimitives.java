package dev.xdark.ssvm.symbol;

import dev.xdark.ssvm.mirror.PrimitiveClass;

/**
 * VM primitives.
 *
 * @author xDark
 */
public interface VMPrimitives {

	 PrimitiveClass longPrimitive();
	 PrimitiveClass doublePrimitive();
	 PrimitiveClass intPrimitive();
	 PrimitiveClass floatPrimitive();
	 PrimitiveClass charPrimitive();
	 PrimitiveClass shortPrimitive();
	 PrimitiveClass bytePrimitive();
	 PrimitiveClass booleanPrimitive();
	 PrimitiveClass voidPrimitive();
}
