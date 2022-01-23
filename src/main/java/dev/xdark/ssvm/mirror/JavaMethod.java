package dev.xdark.ssvm.mirror;

import lombok.val;
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
	private Type[] argumentTypes;
	private Type returnType;
	private Boolean polymorphic;

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

	/**
	 * Returns array of types of arguments.
	 *
	 * @return array of types of arguments.
	 */
	public Type[] getArgumentTypes() {
		Type[] argumentTypes = this.argumentTypes;
		if (argumentTypes == null) {
			argumentTypes = this.argumentTypes = getType().getArgumentTypes();
		}
		return argumentTypes.clone();
	}

	/**
	 * Returns method return type.
	 *
	 * @return method return type.
	 */
	public Type getReturnType() {
		Type returnType = this.returnType;
		if (returnType == null) {
			return this.returnType = getType().getReturnType();
		}
		return returnType;
	}

	/**
	 * @return {@code  true} if this method is polymorphic,
	 * {@code false} otherwise.
	 */
	public boolean isPolymorphic() {
		Boolean polymorphic = this.polymorphic;
		if (polymorphic == null) {
			val visibleAnnotations = node.visibleAnnotations;
			return this.polymorphic = visibleAnnotations != null
					&& visibleAnnotations.stream().anyMatch(x -> "Ljava/lang/invoke/MethodHandle$PolymorphicSignature;".equals(x.desc));
		}
		return polymorphic;
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
		return node.hashCode();
	}

	@Override
	public String toString() {
		val node = this.node;
		return owner.getInternalName() + '.' + node.name + node.desc;
	}
}
