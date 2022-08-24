package dev.xdark.ssvm.asm;

import dev.xdark.ssvm.value.ObjectValue;
import org.objectweb.asm.tree.AbstractInsnNode;

/**
 * VM reference constant.
 *
 * @author xDark
 */
public final class ConstantReferenceInsnNode extends DelegatingInsnNode<AbstractInsnNode> {
	private final ObjectValue value;

	/**
	 * @param delegate      Backing instruction.
	 * @param value         Constant value.
	 */
	public ConstantReferenceInsnNode(AbstractInsnNode delegate, ObjectValue value) {
		super(delegate, VMOpcodes.VM_CONSTANT_REFERENCE);
		this.value = value;
	}

	/**
	 * @return Constant value.
	 */
	public ObjectValue getValue() {
		return value;
	}
}
