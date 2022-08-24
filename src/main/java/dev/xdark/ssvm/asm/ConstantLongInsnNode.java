package dev.xdark.ssvm.asm;

import org.objectweb.asm.tree.AbstractInsnNode;

/**
 * VM long constant.
 *
 * @author xDark
 */
public final class ConstantLongInsnNode extends DelegatingInsnNode<AbstractInsnNode> {
	private final long value;

	/**
	 * @param delegate      Backing instruction.
	 * @param value         Constant value.
	 */
	public ConstantLongInsnNode(AbstractInsnNode delegate, long value) {
		super(delegate, VMOpcodes.VM_CONSTANT_LONG);
		this.value = value;
	}

	/**
	 * @return Constant value.
	 */
	public long getValue() {
		return value;
	}
}
