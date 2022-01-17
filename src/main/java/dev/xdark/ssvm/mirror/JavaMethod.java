package dev.xdark.ssvm.mirror;

import org.objectweb.asm.Type;
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
	private Type type;

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

	/**
	 * Returns method name.
	 *
	 * @return method name.
	 */
	public String getName() {
		return node.name;
	}

	/**
	 * Returns method descriptor.
	 *
	 * @return method descriptor.
	 */
	public String getDesc() {
		return node.desc;
	}

	/**
	 * Returns method access.
	 *
	 * @return method access.
	 */
	public int getAccess() {
		return node.access;
	}

	/**
	 * Returns method signature.
	 *
	 * @return method signature.
	 */
	public String getSignature() {
		return node.signature;
	}

	/**
	 * Returns method type.
	 *
	 * @return method type.
	 */
	public Type getType() {
		Type type = this.type;
		if (type == null) {
			return this.type = Type.getMethodType(node.desc);
		}
		return type;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		JavaMethod that = (JavaMethod) o;

		if (!owner.equals(that.owner)) return false;
		return node.equals(that.node);
	}

	@Override
	public int hashCode() {
		int result = owner.hashCode();
		result = 31 * result + node.hashCode();
		return result;
	}
}
