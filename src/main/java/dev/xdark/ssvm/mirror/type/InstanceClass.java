package dev.xdark.ssvm.mirror.type;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.mirror.member.JavaField;
import dev.xdark.ssvm.mirror.member.JavaMethod;
import dev.xdark.ssvm.mirror.member.area.ClassArea;
import me.coley.cafedude.classfile.ClassFile;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

import java.util.List;

/**
 * Class representing Java class of instance values.
 *
 * @apiNote xDark
 */
public interface InstanceClass extends JavaClass {

	/**
	 * @return VM in which this class was created.
	 */
	VirtualMachine getVM();

	/**
	 * Searches for a field by it's name and descriptor.
	 *
	 * @param name Name of the field.
	 * @param desc Descriptor of the field.
	 * @return virtual class field or {@code null}, if not found.
	 */
	JavaField getField(String name, String desc);

	/**
	 * Searches for a method by it's name and descriptor.
	 *
	 * @param name Name of the method.
	 * @param desc Descriptor of the method.
	 * @return class method or {@code null}, if not found.
	 */
	JavaMethod getMethod(String name, String desc);

	/**
	 * Returns ASM node.
	 *
	 * @return asm node.
	 */
	ClassNode getNode();

	/**
	 * Returns class source.
	 *
	 * @return class source.
	 */
	ClassReader getClassReader();

	/**
	 * Returns list of all methods.
	 *
	 * @param publicOnly Should only public methods be included.
	 * @return all methods.
	 */
	List<JavaMethod> getDeclaredMethods(boolean publicOnly);

	/**
	 * Returns list of all constructors.
	 *
	 * @param publicOnly Should only public constructors be included.
	 * @return all constructors.
	 */
	List<JavaMethod> getDeclaredConstructors(boolean publicOnly);

	/**
	 * Returns list of all fields.
	 *
	 * @param publicOnly Should only public fields be included.
	 * @return all fields.
	 */
	List<JavaField> getDeclaredFields(boolean publicOnly);

	/**
	 * @return raw class file.
	 */
	ClassFile getRawClassFile();

	/**
	 * @return {@code true} if the class should be initialized,
	 * {@code false} otherwise.
	 */
	boolean shouldBeInitialized();

	/**
	 * @param slot Method slot.
	 * @return method by it's slot.
	 */
	JavaMethod getMethodBySlot(int slot);

	/**
	 * @param slot Field slot.
	 * @return field by it's slot.
	 */
	JavaField getFieldBySlot(int slot);

	/**
	 * @return Area of methods.
	 */
	ClassArea<JavaMethod> methodArea();

	/**
	 * @return Area of virtual fields.
	 */
	ClassArea<JavaField> virtualFieldArea();

	/**
	 * @return Area of static fields.
	 */
	ClassArea<JavaField> staticFieldArea();

	/**
	 * @return The amount of bytes requires to
	 * allocate an object of this type.
	 */
	long getOccupiedInstanceSpace();

	/**
	 * @return The amount of bytes requires to
	 * store all static fields for this class.
	 */
	long getOccupiedStaticSpace();

	/**
	 * Attempts to redefine this class.
	 *
	 * @param reader Class source.
	 * @param node   New class node.
	 * @throws IllegalStateException If any methods or fields were added, removed, or order has changed.
	 *                               If modifiers of fields or methods were changed (method/field became (non-) static).
	 */
	void redefine(ClassReader reader, ClassNode node);

	/**
	 * @return {@code true} if an instance of a type of
	 * this class can be allocated.
	 */
	boolean canAllocateInstance();

	/**
	 * @return Initialization state.
	 */
	InitializationState state();

	/**
	 * @return Class linkage bridge.
	 */
	ClassLinkage linkage();

	enum State {
		PENDING,
		IN_PROGRESS,
		COMPLETE,
		FAILED,
	}
}
