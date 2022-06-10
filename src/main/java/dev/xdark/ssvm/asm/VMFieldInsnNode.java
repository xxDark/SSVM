package dev.xdark.ssvm.asm;

import dev.xdark.ssvm.mirror.JavaField;
import dev.xdark.ssvm.mirror.JavaMethod;
import org.objectweb.asm.tree.FieldInsnNode;

/**
 * VM field instruction.
 *
 * @author xDark
 */
public final class VMFieldInsnNode extends DelegatingInsnNode<FieldInsnNode> {

	private JavaField resolved;

	/**
	 * @param delegate      Backing instruction.
	 * @param virtualOpcode VM specific opcode.
	 */
	public VMFieldInsnNode(FieldInsnNode delegate, int virtualOpcode) {
		super(delegate, virtualOpcode);
	}

	/**
	 * @return resolved field.
	 */
	public JavaField getResolved() {
		return resolved;
	}

	/**
	 * @param resolved New resolved field.
	 */
	public void setResolved(JavaField resolved) {
		this.resolved = resolved;
	}
}
