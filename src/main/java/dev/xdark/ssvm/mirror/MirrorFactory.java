package dev.xdark.ssvm.mirror;

import dev.xdark.ssvm.value.ObjectValue;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Mirror factory.
 *
 * @author xDark
 */
public interface MirrorFactory {

	/**
	 * @param classLoader Class loader.
	 * @param classReader Class source.
	 * @param node        Class node.
	 * @return new class.
	 */
	InstanceJavaClass newInstanceClass(ObjectValue classLoader, ClassReader classReader, ClassNode node);

	/**
	 * @param name       Primitive name.
	 * @param descriptor Primitive descriptor.
	 * @param sort       Primitive sort.
	 * @return new class.
	 */
	PrimitiveClass newPrimitiveClass(String name, String descriptor, int sort);

	/**
	 * @param owner  Field owner.
	 * @param node   Field node.
	 * @param slot   Field slot.
	 * @param offset Field offset.
	 * @return new field.
	 */
	JavaField newField(InstanceJavaClass owner, FieldNode node, int slot, long offset);

	/**
	 * @param owner Method owner.
	 * @param node  Method node.
	 * @param slot  Method slot.
	 * @return new method.
	 */
	JavaMethod newMethod(InstanceJavaClass owner, MethodNode node, int slot);

	/**
	 * @param method Original method.
	 * @param desc   New descriptor.
	 * @return new method.
	 */
	JavaMethod newPolymorphicMethod(JavaMethod method, String desc);
}
