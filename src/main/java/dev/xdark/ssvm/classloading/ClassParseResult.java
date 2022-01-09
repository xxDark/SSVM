package dev.xdark.ssvm.classloading;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

/**
 * Result of class parsing.
 *
 * @author xDark
 */
public final class ClassParseResult {

	private final ClassReader classReader;
	private final ClassNode node;

	/**
	 * @param classReader
	 * 		Class source.
	 * @param node
	 * 		ASM node.
	 */
	public ClassParseResult(ClassReader classReader, ClassNode node) {
		this.classReader = classReader;
		this.node = node;
	}

	/**
	 * Returns origin class reader.
	 *
	 * @return class reader.
	 */
	public ClassReader getClassReader() {
		return classReader;
	}

	/**
	 * Returns ASM node.
	 *
	 * @return asm node.
	 */
	public ClassNode getNode() {
		return node;
	}
}
