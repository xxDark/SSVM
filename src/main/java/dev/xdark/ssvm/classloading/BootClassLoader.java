package dev.xdark.ssvm.classloading;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

/**
 * Boot class loader interface for the VM.
 *
 * @author xDark
 */
public interface BootClassLoader {

	/**
	 * Resolves boot class by it's name.
	 *
	 * @param name
	 * 		Name of the class.
	 *
	 * @return Resolved class or {@code null}, if not found.
	 *
	 * @throws Exception
	 * 		if any error occurs.
	 */
	LookupResult findBootClass(String name) throws Exception;

	/**
	 * Result of the boot class lookup.
	 */
	final class LookupResult {

		private final ClassReader classReader;
		private final ClassNode node;

		/**
		 * @param classReader
		 * 		Class source.
		 * @param node
		 * 		ASM node.
		 */
		public LookupResult(ClassReader classReader, ClassNode node) {
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
}
