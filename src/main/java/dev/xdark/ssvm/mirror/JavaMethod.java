package dev.xdark.ssvm.mirror;

import org.objectweb.asm.tree.MethodNode;

/**
 * Method info.
 *
 * @author xDark
 */
public final class JavaMethod {

	private final InstanceJavaClass owner;
	private final MethodNode node;
	private final int slot;

	/**
	 * @param owner
	 * 		Method owner.
	 * @param node
	 * 		ASM method info.
	 * @param slot
	 * 		Method slot.
	 */
	public JavaMethod(InstanceJavaClass owner, MethodNode node, int slot) {
		this.owner = owner;
		this.node = node;
		this.slot = slot;
	}

	/**
	 * Returns method owner.
	 *
	 * @return method owner.
	 */
	public InstanceJavaClass getOwner() {
		return owner;
	}

	/**
	 * Returns ASM method info.
	 *
	 * @return ASM method info.
	 */
	public MethodNode getNode() {
		return node;
	}

	/**
	 * Returns method slot.
	 *
	 * @return method slot.
	 */
	public int getSlot() {
		return slot;
	}
}
