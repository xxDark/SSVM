package dev.xdark.ssvm.mirror;

import dev.xdark.ssvm.value.Value;

/**
 * VM representation of Java class.
 *
 * @author xDark
 */
public interface JavaClass {

	/**
	 * Returns name of the class.
	 *
	 * @return name of the class.
	 */
	String getName();

	/**
	 * Returns internal name of the class.
	 *
	 * @return internal name of the class.
	 */
	String getInternalName();

	/**
	 * Returns descriptor of the class.
	 *
	 * @return descriptor of the class.
	 */
	String getDescriptor();

	/**
	 * Returns access modifiers of the class.
	 *
	 * @return access modifiers of the class.
	 */
	int getModifiers();

	/**
	 * Returns class loader of this class.
	 *
	 * @return class loader.
	 */
	Value getClassLoader();

	/**
	 * Returns VM representation of this class.
	 *
	 * @return oop.
	 */
	Value getOop();

	/**
	 * Returns class layout.
	 *
	 * @return class layout.
	 */
	ClassLayout getLayout();
}
