package dev.xdark.ssvm.asm;

import dev.xdark.ssvm.value.Value;
import lombok.Getter;
import org.objectweb.asm.tree.LdcInsnNode;

/**
 * Represents VM ldc instruction.
 *
 * @author xDark
 */
public final class VMLdcInsnNode extends DelegatingInsnNode<LdcInsnNode> {

	@Getter
	private final Value value;

	/**
	 * @param delegate Backing instruction.
	 * @param value    VM value.
	 */
	public VMLdcInsnNode(LdcInsnNode delegate, Value value) {
		super(delegate, VMOpcodes.LDC);
		this.value = value;
	}
}
