package dev.xdark.ssvm.asm;

import org.objectweb.asm.tree.AbstractInsnNode;

/**
 * VM double constant.
 *
 * @author xDark
 */
public final class ConstantDoubleInsnNode extends DelegatingInsnNode<AbstractInsnNode> {
	private final double value;

	/**
	 * @param delegate      Backing instruction.
	 * @param value         Constant value.
	 */
	public ConstantDoubleInsnNode(AbstractInsnNode delegate, double value) {
		super(delegate, VMOpcodes.VM_CONSTANT_DOUBLE);
		this.value = value;
	}

	/**
	 * @return Constant value.
	 */
	public double getValue() {
		return value;
	}
}
