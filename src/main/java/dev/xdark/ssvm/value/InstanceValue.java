package dev.xdark.ssvm.value;

import dev.xdark.ssvm.mirror.InstanceJavaClass;

/**
 * Represents instance value.
 * (Arrays are represent differently).
 *
 * @author xDark
 */
public interface InstanceValue extends ObjectValue {

	@Override
	InstanceJavaClass getJavaClass();

	/**
	 * Returns VM value of a field.
	 *
	 * @param field Field name.
	 * @return VM value.
	 */
	@Deprecated
	ObjectValue getValue(String field, String desc);

	/**
	 * Marks this object as initialized.
	 */
	void initialize();

	/**
	 * @param name Field name.
	 * @param desc Field desc.
	 * @return field offset for this object or {@code -1},
	 * if not found.
	 */
	long getFieldOffset(String name, String desc);
}
