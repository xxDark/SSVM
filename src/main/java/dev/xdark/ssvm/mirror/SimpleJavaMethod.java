package dev.xdark.ssvm.mirror;

import dev.xdark.ssvm.util.AsmUtil;
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
public final class SimpleJavaMethod implements JavaMethod {

	private final InstanceJavaClass owner;
	private final MethodNode node;
	private final String desc;
	private final int slot;
	private Type type;
	private Type[] argumentTypes;
	private Type returnType;
	private Boolean polymorphic;
	private int maxArgs = -1;
	private int maxLocals = -1;
	private int invocationCount;
	private Boolean callerSensitive;
	private Boolean isConstructor;

	/**
	 * @param owner Method owner.
	 * @param node  ASM method info.
	 * @param desc  Method descriptor override.
	 * @param slot  Method slot.
	 */
	public SimpleJavaMethod(InstanceJavaClass owner, MethodNode node, String desc, int slot) {
		this.owner = owner;
		this.node = node;
		this.desc = desc;
		this.slot = slot;
	}

	@Override
	public InstanceJavaClass getOwner() {
		return owner;
	}

	@Override
	public MethodNode getNode() {
		return node;
	}

	@Override
	public int getSlot() {
		return slot;
	}

	@Override
	public Type getType() {
		Type type = this.type;
		if (type == null) {
			return this.type = Type.getMethodType(desc);
		}
		return type;
	}

	@Override
	public Type[] getArgumentTypes() {
		Type[] argumentTypes = this.argumentTypes;
		if (argumentTypes == null) {
			argumentTypes = this.argumentTypes = getType().getArgumentTypes();
		}
		return argumentTypes.clone();
	}

	@Override
	public Type getReturnType() {
		Type returnType = this.returnType;
		if (returnType == null) {
			return this.returnType = getType().getReturnType();
		}
		return returnType;
	}

	@Override
	public boolean isPolymorphic() {
		Boolean polymorphic = this.polymorphic;
		if (polymorphic == null) {
			List<AnnotationNode> visibleAnnotations = node.visibleAnnotations;
			return this.polymorphic = visibleAnnotations != null
				&& visibleAnnotations.stream().anyMatch(x -> "Ljava/lang/invoke/MethodHandle$PolymorphicSignature;".equals(x.desc));
		}
		return polymorphic;
	}

	@Override
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

	@Override
	public int getMaxStack() {
		return node.maxStack;
	}

	@Override
	public int getMaxLocals() {
		int maxLocals = this.maxLocals;
		if (maxLocals == -1) {
			return this.maxLocals = AsmUtil.getMaxLocals(this);
		}
		return maxLocals;
	}

	@Override
	public int getInvocationCount() {
		return invocationCount;
	}

	@Override
	public void increaseInvocation() {
		invocationCount++;
	}

	@Override
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
	public boolean isConstructor() {
		Boolean isConstructor = this.isConstructor;
		if (isConstructor == null) {
			return this.isConstructor = "<init>".equals(getName());
		}
		return isConstructor;
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

		return node.equals(that.getNode());
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
