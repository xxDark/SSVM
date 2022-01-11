package dev.xdark.ssvm.mirror;

import org.objectweb.asm.tree.FieldNode;

/**
 * Field info.
 *
 * @author xDark
 */
public final class JavaField {

	private final InstanceJavaClass owner;
	private final FieldNode node;
	private final int slot;
	private final long offset;

	/**
	 * @param owner
	 * 		Method owner.
	 * @param node
	 * 		ASM field info.
	 * @param slot
	 * 		Field slot.
	 * @param offset
	 * 		Field offset.
	 */
	public JavaField(InstanceJavaClass owner, FieldNode node, int slot, long offset) {
		this.owner = owner;
		this.node = node;
		this.slot = slot;
		this.offset = offset;
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
	 * Returns ASM field info.
	 *
	 * @return ASM field info.
	 */
	public FieldNode getNode() {
		return node;
	}

	/**
	 * Returns field slot.
	 *
	 * @return field slot.
	 */
	public int getSlot() {
		return slot;
	}

	/**
	 * Returns field offset.
	 *
	 * @return field offset.
	 */
	public long getOffset() {
		return offset;
	}
}
