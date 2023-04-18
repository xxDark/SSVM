package dev.xdark.ssvm.asm;

import org.objectweb.asm.tree.AbstractInsnNode;

/**
 * VM float constant.
 *
 * @author xDark
 */
public final class ConstantFloatInsnNode extends DelegatingInsnNode<AbstractInsnNode> {
	private final float value;

	/**
	 * @param delegate      Backing instruction.
	 * @param value         Constant value.
	 */
	public ConstantFloatInsnNode(AbstractInsnNode delegate, float value) {
		super(delegate, VMOpcodes.VM_CONSTANT_FLOAT);
		this.value = value;
	}

	/**
	 * @return Constant value.
	 */
	public float getValue() {
		return value;
	}
}
