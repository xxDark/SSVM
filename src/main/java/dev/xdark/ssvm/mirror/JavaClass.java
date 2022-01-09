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

	/**
	 * Returns parent class of this class.
	 *
	 * @return parent class.
	 */
	JavaClass getSuperClass();

	/**
	 * Returns interfaces of this class.
	 *
	 * @return interfaces of this class.
	 */
	JavaClass[] getInterfaces();

	/**
	 * Creates new array class.
	 *
	 * @return new array class.
	 */
	ArrayJavaClass newArrayClass();

	/**
	 * Initializes class data.
	 */
	void initialize();

	/**
	 * @param other
	 * 		The Class object to be checked.
	 *
	 * @return {@code boolean} value indicating whether objects
	 * of the type cls can be assigned to objects of this class.
	 *
	 * @see Class#isAssignableFrom(Class)
	 */
	boolean isAssignableFrom(JavaClass other);

	/**
	 * Returns whether this class
	 * is primitive or not.
	 *
	 * @return {@code true} if this class is primitive,
	 * {@code false} otherwise.
	 */
	boolean isPrimitive();

	/**
	 * Returns whether this class
	 * is an array or not.
	 *
	 * @return {@code true} if this class is an array,
	 * {@code false} otherwise.
	 */
	boolean isArray();

	/**
	 * Returns whether this class
	 * is an interface or not.
	 *
	 * @return {@code true} if this class is an interface,
	 * {@code false} otherwise.
	 */
	boolean isInterface();

}
