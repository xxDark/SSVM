package dev.xdark.ssvm.asm;

import org.objectweb.asm.tree.AbstractInsnNode;

/**
 * VM int constant.
 *
 * @author xDark
 */
public final class ConstantIntInsnNode extends DelegatingInsnNode<AbstractInsnNode> {
	private final int value;

	/**
	 * @param delegate      Backing instruction.
	 * @param value         Constant value.
	 */
	public ConstantIntInsnNode(AbstractInsnNode delegate, int value) {
		super(delegate, VMOpcodes.VM_CONSTANT_INT);
		this.value = value;
	}

	/**
	 * @return Constant value.
	 */
	public int getValue() {
		return value;
	}
}
