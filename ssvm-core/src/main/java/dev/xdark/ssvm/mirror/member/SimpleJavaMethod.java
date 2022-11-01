package dev.xdark.ssvm.mirror.member;

import dev.xdark.jlinker.MemberInfo;
import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.asm.Modifier;
import dev.xdark.ssvm.execution.VMTryCatchBlock;
import dev.xdark.ssvm.mirror.type.InstanceClass;
import dev.xdark.ssvm.mirror.type.JavaClass;
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
	private final InstanceClass owner;
	private final MethodNode node;
	private final String desc;
	private final int slot;
	private Type type;
	private JavaClass[] argumentTypes;
	private JavaClass returnType;
	private JavaClass[] exceptionTypes;
	private Boolean polymorphic;
	private int maxArgs = -1;
	private int maxLocals = -1;
	private int invocationCount;
	private Boolean callerSensitive;
	private Boolean hidden;
	private Boolean isConstructor;
	private List<VMTryCatchBlock> tryCatchBlocks;
	private MemberIdentifier identifier;
	private MemberInfo<JavaMethod> linkerInfo; // Delayed allocation until linker is capable of linking polymorphic methods.

	/**
	 * @param owner Method owner.
	 * @param node  ASM method info.
	 * @param desc  Method descriptor override.
	 * @param slot  Method slot.
	 */
	public SimpleJavaMethod(InstanceClass owner, MethodNode node, String desc, int slot) {
		this.owner = owner;
		this.node = node;
		this.desc = desc;
		this.slot = slot;
	}

	@Override
	public InstanceClass getOwner() {
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
	public JavaClass[] getExceptionTypes() {
		JavaClass[] exceptionTypes = this.exceptionTypes;
		if (exceptionTypes == null) {
			resolveExceptionTypes();
			return this.exceptionTypes;
		}
		return exceptionTypes;
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
			if (!owner.getClassLoader().isNull()) {
				return this.callerSensitive = false;
			}
			List<AnnotationNode> visibleAnnotations = node.visibleAnnotations;
			return this.callerSensitive = visibleAnnotations != null
				&& visibleAnnotations.stream().anyMatch(x -> {
				String desc = x.desc;
				return "Lsun/reflect/CallerSensitive;".equals(desc) || "Ljdk/internal/reflect/CallerSensitive;".equals(desc);
			});
		}
		return callerSensitive;
	}

	@Override
	public boolean isHidden() {
		Boolean hidden = this.hidden;
		if (hidden == null) {
			if (!owner.getClassLoader().isNull()) {
				return this.hidden = false;
			}
			List<AnnotationNode> visibleAnnotations = node.visibleAnnotations;
			return this.hidden = visibleAnnotations != null
				&& visibleAnnotations.stream().anyMatch(x -> {
				String desc = x.desc;
				return "L/java/lang/invoke/MethodHandle$PolymorphicSignature;".equals(desc) || "Ljava/lang/invoke/LambdaForm$Hidden;".equals(desc);
			});
		}
		return false;
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
	public MemberIdentifier getIdentifier() {
		MemberIdentifier key = this.identifier;
		if (key == null) {
			return this.identifier = new SimpleMemberIdentifier(getName(), desc);
		}
		return key;
	}

	@Override
	public int getSlot() {
		return slot;
	}

	@Override
	public MemberInfo<? extends JavaMember> linkerInfo() {
		MemberInfo<JavaMethod> linkerInfo = this.linkerInfo;
		if (linkerInfo == null) {
			linkerInfo = makeLinkerInfo(this);
			this.linkerInfo = linkerInfo;
		}
		return linkerInfo;
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
			InstanceClass owner = this.owner;
			VirtualMachine vm = owner.getVM();
			tryCatchBlocks = blocks.stream()
				.map(x -> new VMTryCatchBlock(x.start, x.end, x.handler, x.type, vm, owner))
				.collect(Collectors.toList());
		}
		this.tryCatchBlocks = tryCatchBlocks;
		return tryCatchBlocks;
	}

	private void resolveReturnType() {
		InstanceClass owner = this.owner;
		VirtualMachine vm = owner.getVM();
		returnType = vm.getOperations().findClass(owner, getType().getReturnType(), false);
	}

	private void resolveArgumentTypes() {
		InstanceClass owner = this.owner;
		VirtualMachine vm = owner.getVM();
		Type[] types = getType().getArgumentTypes();
		JavaClass[] arr = new JavaClass[types.length];
		for (int i = 0; i < types.length; i++) {
			arr[i] = vm.getOperations().findClass(owner, types[i], false);
		}
		argumentTypes = arr;
	}

	private void resolveExceptionTypes() {
		InstanceClass owner = this.owner;
		VirtualMachine vm = owner.getVM();
		List<String> exceptions = getNode().exceptions;
		if (exceptions == null || exceptions.isEmpty()) {
			exceptionTypes = new JavaClass[0];
		} else {
			JavaClass[] arr = new JavaClass[exceptions.size()];
			for (int i = 0; i < exceptions.size(); i++) {
				arr[i] = vm.getOperations().findClass(owner, exceptions.get(i), false);
			}
			exceptionTypes = arr;
		}
	}

	private static MemberInfo<JavaMethod> makeLinkerInfo(JavaMethod method) {
		return new MemberInfo<JavaMethod>() {
			@Override
			public JavaMethod innerValue() {
				return method;
			}

			@Override
			public int accessFlags() {
				return Modifier.eraseMethod(method.getModifiers());
			}

			@Override
			public boolean isPolymorphic() {
				return method.isPolymorphic();
			}
		};
	}
}
