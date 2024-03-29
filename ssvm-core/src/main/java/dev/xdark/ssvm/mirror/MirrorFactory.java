package dev.xdark.ssvm.mirror;

import dev.xdark.ssvm.mirror.member.JavaField;
import dev.xdark.ssvm.mirror.member.JavaMethod;
import dev.xdark.ssvm.mirror.type.ArrayClass;
import dev.xdark.ssvm.mirror.type.InstanceClass;
import dev.xdark.ssvm.mirror.type.JavaClass;
import dev.xdark.ssvm.mirror.type.PrimitiveClass;
import dev.xdark.ssvm.value.ObjectValue;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;
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
	 * @return New class.
	 */
	InstanceClass newInstanceClass(ObjectValue classLoader, ClassReader classReader, ClassNode node);

	/**
	 * @param type Primitive type.
	 * @return New primitive class.
	 */
	PrimitiveClass newPrimitiveClass(Type type);

	/**
	 * @param componentType Array component type.
	 * @return New array class.
	 */
	ArrayClass newArrayClass(JavaClass componentType);

	/**
	 * @param owner  Field owner.
	 * @param node   Field node.
	 * @param slot   Field slot.
	 * @param offset Field offset.
	 * @return New field.
	 */
	JavaField newField(InstanceClass owner, FieldNode node, int slot, long offset);

	/**
	 * @param owner Method owner.
	 * @param node  Method node.
	 * @param slot  Method slot.
	 * @return New method.
	 */
	JavaMethod newMethod(InstanceClass owner, MethodNode node, int slot);

	/**
	 * @param method Original method.
	 * @param desc   New descriptor.
	 * @return New method.
	 */
	JavaMethod newPolymorphicMethod(JavaMethod method, String desc);
}
