package dev.xdark.ssvm.inject;

import dev.xdark.ssvm.asm.Modifier;
import org.objectweb.asm.tree.FieldNode;

/**
 * Injected field.
 *
 * @author xDark
 */
public final class InjectedField {

	private final int accessFlags;
	private final String name;
	private final String descriptor;

	/**
	 * @param accessFlags     Access flags.
	 * @param name       Field name.
	 * @param descriptor Field descriptor.
	 */
	InjectedField(int accessFlags, String name, String descriptor) {
		this.accessFlags = accessFlags;
		this.name = name;
		this.descriptor = descriptor;
	}

	/**
	 * @return Access flags.
	*/
	public int access() {
		return accessFlags;
	}

	/**
	 * @return Field name.
	 */
	public String name() {
		return name;
	}

	/**
	 * @return Field descriptor.
	 */
	public String descriptor() {
		return descriptor;
	}

	/**
	 * @return New field node.
	 */
	public FieldNode newNode() {
		return new FieldNode(
			accessFlags | Modifier.ACC_VM_HIDDEN,
			name,
			descriptor,
			null,
			null
		);
	}
}
