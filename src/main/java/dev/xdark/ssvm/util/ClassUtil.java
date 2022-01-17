package dev.xdark.ssvm.util;

import lombok.experimental.UtilityClass;
import lombok.val;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

/**
 * {@link ClassNode} utils.
 *
 * @author xDark
 */
@UtilityClass
public class ClassUtil {

	/**
	 * Reads node from {@link ClassReader}.
	 * <p>
	 * This method will ignore stackmap of the class.
	 *
	 * @param reader
	 * 		Source to read from.
	 *
	 * @return read node.
	 */
	public ClassNode readNode(ClassReader reader) {
		val node = new ClassNode();
		reader.accept(node, ClassReader.SKIP_FRAMES);
		return node;
	}
}
