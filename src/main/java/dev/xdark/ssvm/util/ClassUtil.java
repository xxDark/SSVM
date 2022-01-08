package dev.xdark.ssvm.util;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

/**
 * {@link ClassNode} utils.
 *
 * @author xDark
 */
public final class ClassUtil {

	private ClassUtil() {
	}

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
	public static ClassNode readNode(ClassReader reader) {
		var node = new ClassNode();
		reader.accept(node, ClassReader.SKIP_FRAMES);
		return node;
	}
}
