package dev.xdark.ssvm.mirror;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.List;

/**
 * Method info.
 *
 * @author xDark
 */
public final class JavaMethod {

	private final InstanceJavaClass owner;
	private final MethodNode node;
	private final String desc;
	private final int slot;
	private Type type;
	private Type[] argumentTypes;
	private Type returnType;
	private Boolean polymorphic;
	private int maxArgs = -1;
	private int invocationCount;
	private Boolean callerSensitive;

	/**
	 * @param owner
	 * 		Method owner.
	 * @param node
	 * 		ASM method info.
	 * @param desc
	 * 		Method descriptor override.
	 * @param slot
	 * 		Method slot.
	 */
	public JavaMethod(InstanceJavaClass owner, MethodNode node, String desc, int slot) {
		this.owner = owner;
		this.node = node;
		this.desc = desc;
		this.slot = slot;
	}

	/**
	 * @param owner
	 * 		Method owner.
	 * @param node
	 * 		ASM method info.
	 * @param slot
	 * 		Method slot.
	 */
	public JavaMethod(InstanceJavaClass owner, MethodNode node, int slot) {
		this(owner, node, node.desc, slot);
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
		return desc;
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
			return this.type = Type.getMethodType(desc);
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
			List<AnnotationNode> visibleAnnotations = node.visibleAnnotations;
			return this.polymorphic = visibleAnnotations != null
					&& visibleAnnotations.stream().anyMatch(x -> "Ljava/lang/invoke/MethodHandle$PolymorphicSignature;".equals(x.desc));
		}
		return polymorphic;
	}

	/**
	 * @return the maximum amount of arguments,
	 * including {@code this}.
	 */
	public int getMaxArgs() {
		int maxArgs = this.maxArgs;
		if (maxArgs == -1) {
			int x = 0;
			if ((node.access & Opcodes.ACC_STATIC) == 0) {
				x++;
			}
			for (Type t : getArgumentTypes()) {
				x += t.getSize();
			}
			return this.maxArgs = x;
		}
		return maxArgs;
	}

	/**
	 * @return amount of times
	 * this method was invoked.
	 */
	public int getInvocationCount() {
		return invocationCount;
	}

	/**
	 * Increases invocation count.
	 */
	public void increaseInvocation() {
		invocationCount++;
	}

	/**
	 * @return {@code  true} if this method is caller sensitive,
	 * {@code false} otherwise.
	 */
	public boolean isCallerSensitive() {
		Boolean callerSensitive = this.callerSensitive;
		if (callerSensitive == null) {
			List<AnnotationNode> visibleAnnotations = node.visibleAnnotations;
			return this.callerSensitive = visibleAnnotations != null
					&& visibleAnnotations.stream().anyMatch(x -> "Lsun/reflect/CallerSensitive;".equals(x.desc));
		}
		return callerSensitive;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		JavaMethod that = (JavaMethod) o;

		if (!owner.equals(that.owner)) {
			return false;
		}
		return node.equals(that.node);
	}

	@Override
	public int hashCode() {
		return node.hashCode();
	}

	@Override
	public String toString() {
		MethodNode node = this.node;
		return owner.getInternalName() + '.' + node.name + desc;
	}
}
