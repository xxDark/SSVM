package dev.xdark.ssvm.mirror;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.asm.Modifier;
import dev.xdark.ssvm.execution.VMTryCatchBlock;
import dev.xdark.ssvm.util.AsmUtil;
import dev.xdark.ssvm.util.TypeSafeMap;
import dev.xdark.ssvm.value.ObjectValue;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TryCatchBlockNode;

import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Method info.
 *
 * @author xDark
 */
public final class SimpleJavaMethod implements JavaMethod {

	private final TypeSafeMap metadata = new TypeSafeMap();
	private final BitSet extraModifiers = new BitSet();
	private final InstanceJavaClass owner;
	private final MethodNode node;
	private final String desc;
	private final int slot;
	private Type type;
	private JavaClass[] argumentTypes;
	private JavaClass returnType;
	private Boolean polymorphic;
	private int maxArgs = -1;
	private int maxLocals = -1;
	private int invocationCount;
	private Boolean callerSensitive;
	private Boolean isConstructor;
	private List<VMTryCatchBlock> tryCatchBlocks;

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
	public String getDesc() {
		return desc;
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
	public JavaClass[] getArgumentTypes() {
		JavaClass[] argumentTypes = this.argumentTypes;
		if (argumentTypes == null) {
			resolveArgumentTypes();
			return this.argumentTypes;
		}
		return argumentTypes;
	}

	@Override
	public JavaClass getReturnType() {
		JavaClass returnType = this.returnType;
		if (returnType == null) {
			resolveReturnType();
			return this.returnType;
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
			for (JavaClass arg : getArgumentTypes()) {
				x += arg.getType().getSize();
			}
			return this.maxArgs = x;
		}
		return maxArgs;
	}

	@Override
	public int getMaxStack() {
		MethodNode node = this.node;
		if (Modifier.isCompiledMethod(node.access)) {
			return 0; // No stack for compiled methods
		}
		return node.maxStack;
	}

	@Override
	public int getMaxLocals() {
		if (Modifier.isCompiledMethod(node.access)) {
			return getMaxArgs(); // No locals for compiled methods, except arguments
		}
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
	public List<VMTryCatchBlock> getTryCatchBlocks() {
		List<VMTryCatchBlock> tryCatchBlocks = this.tryCatchBlocks;
		if (tryCatchBlocks == null) {
			tryCatchBlocks = resolveTryCatchBlocks();
		}
		return tryCatchBlocks;
	}

	@Override
	public TypeSafeMap getMetadata() {
		return metadata;
	}

	@Override
	public BitSet extraModifiers() {
		return extraModifiers;
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

	private List<VMTryCatchBlock> resolveTryCatchBlocks() {
		List<VMTryCatchBlock> tryCatchBlocks;
		List<TryCatchBlockNode> blocks = node.tryCatchBlocks;
		if (blocks == null) {
			tryCatchBlocks = Collections.emptyList();
		} else {
			InstanceJavaClass owner = this.owner;
			ObjectValue loader = owner.getClassLoader();
			VirtualMachine vm = owner.getVM();
			tryCatchBlocks = blocks.stream()
				.map(x -> new VMTryCatchBlock(x.start, x.end, x.handler, x.type, vm, loader))
				.collect(Collectors.toList());
		}
		this.tryCatchBlocks = tryCatchBlocks;
		return tryCatchBlocks;
	}

	private void resolveReturnType() {
		InstanceJavaClass owner = this.owner;
		VirtualMachine vm = owner.getVM();
		ObjectValue cl = owner.getClassLoader();
		returnType = vm.getHelper().findClass(cl, getType().getReturnType(), false);
	}

	private void resolveArgumentTypes() {
		InstanceJavaClass owner = this.owner;
		VirtualMachine vm = owner.getVM();
		ObjectValue cl = owner.getClassLoader();
		Type[] types = getType().getArgumentTypes();
		argumentTypes = vm.getHelper().convertTypes(cl, types, false);
	}
}
