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
 * @param <I> Type of the delegating instruction.
 * @author xDark
 */
@Getter
public class DelegatingInsnNode<I extends AbstractInsnNode> extends AbstractInsnNode {

	@Delegate(types = AbstractInsnNode.class, excludes = Exclude.class)
	protected final I delegate;

	/**
	 * @param delegate      Backing instruction.
	 * @param virtualOpcode VM specific opcode.
	 */
	public DelegatingInsnNode(I delegate, int virtualOpcode) {
		super(virtualOpcode);
		this.delegate = delegate;
	}

	private interface Exclude {
		int getOpcode();
	}
}
