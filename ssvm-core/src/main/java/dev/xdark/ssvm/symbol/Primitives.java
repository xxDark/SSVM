package dev.xdark.ssvm.symbol;

import dev.xdark.ssvm.mirror.type.PrimitiveClass;

/**
 * VM primitives.
 *
 * @author xDark
 */
public interface Primitives {

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
