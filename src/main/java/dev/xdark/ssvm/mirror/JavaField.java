package dev.xdark.ssvm.mirror;

import org.objectweb.asm.Type;
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
	private Type type;

	/**
	 * @param owner
	 * 		Field owner.
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
	 * Returns field owner.
	 *
	 * @return field owner.
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

	/**
	 * Returns field name.
	 *
	 * @return field name.
	 */
	public String getName() {
		return node.name;
	}

	/**
	 * Returns field descriptor.
	 *
	 * @return field descriptor.
	 */
	public String getDesc() {
		return node.desc;
	}

	/**
	 * Returns field access.
	 *
	 * @return field access.
	 */
	public int getAccess() {
		return node.access;
	}

	/**
	 * Returns field signature.
	 *
	 * @return field signature.
	 */
	public String getSignature() {
		return node.signature;
	}

	/**
	 * Returns field type.
	 *
	 * @return field type.
	 */
	public Type getType() {
		var type = this.type;
		if (type == null) {
			return this.type = Type.getType(node.desc);
		}
		return type;
	}
}
