package dev.xdark.ssvm.asm;

import dev.xdark.ssvm.mirror.JavaField;
import org.objectweb.asm.tree.FieldInsnNode;

/**
 * VM field instruction.
 *
 * @author xDark
 */
public final class VMFieldInsnNode extends DelegatingInsnNode<FieldInsnNode> {

	private final JavaField resolved;

	/**
	 * @param delegate      Backing instruction.
	 * @param virtualOpcode VM specific opcode.
	 * @param resolved      Resolved field.
	 */
	public VMFieldInsnNode(FieldInsnNode delegate, int virtualOpcode, JavaField resolved) {
		super(delegate, virtualOpcode);
		this.resolved = resolved;
	}

	/**
	 * @return Resolved field.
	 */
	public JavaField getResolved() {
		return resolved;
	}
}
