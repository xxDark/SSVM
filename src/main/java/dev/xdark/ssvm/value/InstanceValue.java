package dev.xdark.ssvm.value;

import dev.xdark.ssvm.mirror.type.InstanceClass;

/**
 * Represents instance value.
 * (Arrays are represent differently).
 *
 * @author xDark
 */
public interface InstanceValue extends ObjectValue {

	@Override
	InstanceClass getJavaClass();
}
