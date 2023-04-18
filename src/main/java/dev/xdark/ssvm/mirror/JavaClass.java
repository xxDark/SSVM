package dev.xdark.ssvm.mirror;

import dev.xdark.ssvm.value.InstanceValue;
import dev.xdark.ssvm.value.ObjectValue;
import org.objectweb.asm.Type;

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
	ObjectValue getClassLoader();

	/**
	 * Returns VM representation of this class.
	 *
	 * @return oop.
	 */
	InstanceValue getOop();

	/**
	 * Returns virtual class layout.
	 *
	 * @return virtual class layout.
	 */
	FieldLayout getVirtualFieldLayout();

	/**
	 * Returns static class layout.
	 *
	 * @return static class layout.
	 */
	FieldLayout getStaticFieldLayout();

	/**
	 * Returns parent class of this class.
	 *
	 * @return parent class.
	 */
	InstanceJavaClass getSuperClass();

	/**
	 * Returns interfaces of this class.
	 *
	 * @return interfaces of this class.
	 */
	InstanceJavaClass[] getInterfaces();

	/**
	 * Creates new array class.
	 *
	 * @return new array class.
	 */
	ArrayJavaClass newArrayClass();

	/**
	 * Returns array class if present.
	 *
	 * @return array class.
	 */
	ArrayJavaClass getArrayClass();

	/**
	 * Initializes class data.
	 */
	void initialize();

	/**
	 * @param other The Class object to be checked.
	 * @return {@code boolean} value indicating whether objects
	 * of the type cls can be assigned to objects of this class.
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

	/**
	 * Returns component type of array.
	 *
	 * @return component type of array or {@code null},
	 * if class is not an array.
	 */
	JavaClass getComponentType();

	/**
	 * @return ASM type.
	 */
	Type getType();

	/**
	 * @return ASM sort type.
	 */
	int getSort();
}
