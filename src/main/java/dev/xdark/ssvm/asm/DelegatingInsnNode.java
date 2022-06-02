package dev.xdark.ssvm.asm;

import lombok.Getter;
import lombok.experimental.Delegate;
import org.objectweb.asm.tree.AbstractInsnNode;

/**
 * ASM instruction delegator.
 * <p>
 * VM uses this instruction to rewrite some Java
 * instructions, e.g. INVOKEDYNAMIC.
 *
 * @param <I>
 * 		Type of the delegating instruction.
 *
 * @author xDark
 */
@Getter
public abstract class DelegatingInsnNode<I extends AbstractInsnNode> extends AbstractInsnNode implements VirtualInsnNode {

	@Delegate(types = AbstractInsnNode.class)
	protected final I delegate;
	private final int virtualOpcode;

	/**
	 * @param delegate
	 * 		Backing instruction.
	 * @param virtualOpcode
	 * 		VM specific opcode.
	 */
	protected DelegatingInsnNode(I delegate, int virtualOpcode) {
		super(delegate.getOpcode());
		this.delegate = delegate;
		this.virtualOpcode = virtualOpcode;
	}
}
