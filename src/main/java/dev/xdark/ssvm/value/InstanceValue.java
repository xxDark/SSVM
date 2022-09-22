package dev.xdark.ssvm.value;

import dev.xdark.ssvm.mirror.type.InstanceJavaClass;

/**
 * Represents instance value.
 * (Arrays are represent differently).
 *
 * @author xDark
 */
public interface InstanceValue extends ObjectValue {

	@Override
	InstanceJavaClass getJavaClass();
}
