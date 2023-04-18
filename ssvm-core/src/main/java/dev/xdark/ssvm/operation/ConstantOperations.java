package dev.xdark.ssvm.operation;

import dev.xdark.ssvm.value.ObjectValue;

/**
 * VM constant operations.
 *
 * @author xDark
 */
public interface ConstantOperations {

	/**
	 * @param value Java value to convert to reference.
	 * @return Reference value.
	 */
	ObjectValue referenceValue(Object value);
}
