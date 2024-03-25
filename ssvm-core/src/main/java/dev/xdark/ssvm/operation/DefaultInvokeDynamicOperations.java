package dev.xdark.ssvm.operation;

import dev.xdark.ssvm.RuntimeResolver;
import dev.xdark.ssvm.asm.Modifier;
import dev.xdark.ssvm.classloading.ClassStorage;
import dev.xdark.ssvm.execution.Locals;
import dev.xdark.ssvm.execution.Stack;
import dev.xdark.ssvm.inject.InjectedClassLayout;
import dev.xdark.ssvm.memory.management.MemoryManager;
import dev.xdark.ssvm.memory.management.StringPool;
import dev.xdark.ssvm.mirror.member.JavaField;
import dev.xdark.ssvm.mirror.member.JavaMethod;
import dev.xdark.ssvm.mirror.type.InstanceClass;
import dev.xdark.ssvm.symbol.Symbols;
import dev.xdark.ssvm.thread.ThreadManager;
import dev.xdark.ssvm.thread.ThreadStorage;
import dev.xdark.ssvm.value.ArrayValue;
import dev.xdark.ssvm.value.InstanceValue;
import dev.xdark.ssvm.value.ObjectValue;
import dev.xdark.ssvm.value.sink.ValueSink;
import lombok.RequiredArgsConstructor;
import org.objectweb.asm.ConstantDynamic;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;

import java.util.Objects;

/**
 * Default implementation.
 *
 * @author xDark
 */
@RequiredArgsConstructor
public final class DefaultInvokeDynamicOperations implements InvokeDynamicOperations {

	private final Symbols symbols;
	private final ThreadManager threadManager;
	private final StringPool stringPool;
	private final RuntimeResolver runtimeResolver;
	private final ClassStorage classStorage;
	private final MemoryManager memoryManager;
	private final VMOperations ops;

	@Override
	public InstanceValue linkCall(InvokeDynamicInsnNode insn, InstanceClass caller) {
		Symbols symbols = this.symbols;
		Handle bootstrap = insn.bsm;
		VMOperations ops = this.ops;
		if (bootstrap.getTag() != Opcodes.H_INVOKESTATIC) {
			ops.throwException(symbols.java_lang_IllegalStateException(), "Bootstrap tag is not static");
		}
		InstanceValue linker = ops.linkMethodHandleConstant(caller, bootstrap);

		Object[] $bsmArgs = insn.bsmArgs;
		ArrayValue bsmArgs = ops.allocateArray(symbols.java_lang_Object(), $bsmArgs.length);
		for (int i = 0; i < $bsmArgs.length; i++) {
			bsmArgs.setReference(i, forInvokeDynamicCall(caller, $bsmArgs[i]));
		}

		StringPool stringPool = this.stringPool;
		ArrayValue appendix = ops.allocateArray(symbols.java_lang_Object(), 1);
		InstanceClass natives = symbols.java_lang_invoke_MethodHandleNatives();
		JavaMethod method = natives.getMethod("linkCallSite", "(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/invoke/MemberName;");
		Locals linkArgs;
		if (method == null) {
			// Bogus cp index entry which was removed
			// shortly after it was added, shaking
			method = natives.getMethod("linkCallSite", "(Ljava/lang/Object;ILjava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/invoke/MemberName;");
			linkArgs = threadManager.currentOsThread().getStorage().newLocals(method);
			linkArgs.setReference(0, caller.getOop());
			linkArgs.setInt(1, 0);
			linkArgs.setReference(2, linker);
			linkArgs.setReference(3, stringPool.intern(insn.name));
			linkArgs.setReference(4, ops.methodType(caller, Type.getMethodType(insn.desc)));
			linkArgs.setReference(5, bsmArgs);
			linkArgs.setReference(6, appendix);
		} else {
			linkArgs = threadManager.currentOsThread().getStorage().newLocals(method);
			linkArgs.setReference(0, caller.getOop());
			linkArgs.setReference(1, linker);
			linkArgs.setReference(2, stringPool.intern(insn.name));
			linkArgs.setReference(3, ops.methodType(caller, Type.getMethodType(insn.desc)));
			linkArgs.setReference(4, bsmArgs);
			linkArgs.setReference(5, appendix);
		}

		ops.invokeVoid(method, linkArgs);
		return (InstanceValue) appendix.getReference(0);
	}

	@Override
	public InstanceValue linkDynamic(ConstantDynamic node, InstanceClass caller) {
		Symbols symbols = this.symbols;
		Handle bootstrap = node.getBootstrapMethod();
		VMOperations ops = this.ops;

		InstanceValue bsm = ops.linkMethodHandleConstant(caller, bootstrap);

		ArrayValue bsmArgs = ops.allocateArray(symbols.java_lang_Object(), node.getBootstrapMethodArgumentCount());
		for (int i = 0; i < node.getBootstrapMethodArgumentCount(); i++) {
			Object arg = node.getBootstrapMethodArgument(i);
			bsmArgs.setReference(i, forInvokeDynamicCall(caller, arg));
		}

		InstanceClass natives = symbols.java_lang_invoke_MethodHandleNatives();
		JavaMethod method = natives.getMethod("linkDynamicConstant", "(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
		Locals linkArgs;
		if (method == null) {
			method = natives.getMethod("linkDynamicConstant", "(Ljava/lang/Object;ILjava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");

			linkArgs = threadManager.currentOsThread().getStorage().newLocals(method);

			linkArgs.setReference(0, caller.getOop());
			linkArgs.setInt(1, 0); // condy index
			linkArgs.setReference(2, bsm);
			linkArgs.setReference(3, stringPool.intern(node.getName()));
			linkArgs.setReference(4, ops.findClass(caller, node.getDescriptor(), false).getOop());
			linkArgs.setReference(5, bsmArgs);
		} else {
			linkArgs = threadManager.currentOsThread().getStorage().newLocals(method);
			linkArgs.setReference(0, caller.getOop());
			linkArgs.setReference(1, bsm);
			linkArgs.setReference(2, stringPool.intern(node.getName()));
			linkArgs.setReference(3, ops.findClass(caller, node.getDescriptor(), false).getOop());
			linkArgs.setReference(4, bsmArgs);
		}

		return (InstanceValue) ops.invokeReference(method, linkArgs);
	}

	@Override
	public void dynamicCall(Stack stack, String desc, InstanceValue handle, ValueSink sink) {
		ThreadStorage ts = threadManager.currentOsThread().getStorage();
		if (symbols.java_lang_invoke_CallSite().isAssignableFrom(handle.getJavaClass())) {
			// See linkCallSiteImpl
			JavaMethod getTarget = runtimeResolver.resolveVirtualMethod(handle, "getTarget", "()Ljava/lang/invoke/MethodHandle;");
			Locals locals = ts.newLocals(getTarget);
			locals.setReference(0, handle);
			handle = ops.checkNotNull(ops.invokeReference(getTarget, locals));
		}
		JavaMethod invokeExact = runtimeResolver.resolveVirtualMethod(handle, "invokeExact", desc);
		Locals locals = ts.newLocals(invokeExact);
		locals.setReference(0, handle);
		stack.sinkInto(locals, 1, invokeExact.getMaxArgs() - 1);
		ops.invoke(invokeExact, locals, sink);
	}

	@Override
	public JavaMethod readVMTargetFromHandle(InstanceValue handle) {
		VMOperations ops = this.ops;
		InstanceValue form = ops.checkNotNull(ops.getReference(handle, "form", "Ljava/lang/invoke/LambdaForm;"));
		InstanceValue vmentry = ops.checkNotNull(ops.getReference(form, "vmentry", "Ljava/lang/invoke/MemberName;"));
		return readVMTargetFromMemberName(vmentry);
	}

	@Override
	public JavaMethod readVMTargetFromMemberName(InstanceValue vmentry) {
		VMOperations ops = this.ops;
		InstanceValue resolved = ops.checkNotNull(ops.getReference(vmentry, "method", symbols.java_lang_invoke_ResolvedMethodName().getDescriptor()));
		InstanceClass clazz = (InstanceClass) classStorage.lookup(ops.checkNotNull(ops.getReference(vmentry, "clazz", "Ljava/lang/Class;")));
		return clazz.getMethodBySlot(ops.getInt(ops.getReference(resolved, InjectedClassLayout.java_lang_invoke_ResolvedMethodName_vmtarget.name(), InjectedClassLayout.java_lang_invoke_ResolvedMethodName_vmtarget.descriptor()), "value"));
	}

	/**
	 * Initializes method member.
	 *
	 * @param refKind    Reference kind.
	 * @param memberName Member name instance.
	 * @param handle     Method handle.
	 * @param mnType     Linkage type.
	 */
	public void initMethodMember(int refKind, InstanceValue memberName, JavaMethod handle, int mnType) {
		VMOperations ops = this.ops;
		Symbols symbols = this.symbols;
		// Inject vmholder & vmtarget into resolved name
		ops.putInt(memberName, InjectedClassLayout.java_lang_invoke_MemberName_vmindex.name(), handle.getSlot());
		MemoryManager memoryManager = this.memoryManager;
		InstanceClass rmn = symbols.java_lang_invoke_ResolvedMethodName();
		ops.initialize(rmn);
		InstanceValue resolvedName = memoryManager.newInstance(rmn);
		ops.putReference(resolvedName, InjectedClassLayout.java_lang_invoke_ResolvedMethodName_vmtarget.name(), InjectedClassLayout.java_lang_invoke_ResolvedMethodName_vmtarget.descriptor(), ops.boxInt(handle.getSlot()));
		ops.putReference(resolvedName, InjectedClassLayout.java_lang_invoke_ResolvedMethodName_vmholder.name(), InjectedClassLayout.java_lang_invoke_ResolvedMethodName_vmholder.descriptor(), handle.getOwner().getOop());
		ops.putReference(memberName, "method", symbols.java_lang_invoke_ResolvedMethodName().getDescriptor(), resolvedName);
		// Inject flags
		int flags = handle.getModifiers() & Modifier.RECOGNIZED_METHOD_MODIFIERS;
		flags |= mnType | (refKind << MN_REFERENCE_KIND_SHIFT);
		ops.putInt(memberName, "flags", flags);
	}

	/**
	 * Initializes field member.
	 *
	 * @param refKind    Reference kind.
	 * @param memberName Member name instance.
	 * @param handle     Field handle.
	 */
	public void initFieldMember(int refKind, InstanceValue memberName, JavaField handle) {
		VMOperations ops = this.ops;
		// Inject vmholder & vmtarget into resolved name
		InstanceClass owner = handle.getOwner();
		long offset = handle.getOffset();
		ops.putInt(memberName, InjectedClassLayout.java_lang_invoke_MemberName_vmindex.name(), (int) offset);
		InstanceClass rmn = symbols.java_lang_invoke_ResolvedMethodName();
		ops.initialize(rmn);
		InstanceValue resolvedName = memoryManager.newInstance(rmn);
		ops.putReference(resolvedName, InjectedClassLayout.java_lang_invoke_ResolvedMethodName_vmtarget.name(), InjectedClassLayout.java_lang_invoke_ResolvedMethodName_vmtarget.descriptor(), ops.boxInt(handle.getSlot()));
		ops.putReference(resolvedName, InjectedClassLayout.java_lang_invoke_ResolvedMethodName_vmholder.name(), InjectedClassLayout.java_lang_invoke_ResolvedMethodName_vmholder.descriptor(), owner.getOop());
		ops.putReference(memberName, "method", symbols.java_lang_invoke_ResolvedMethodName().getDescriptor(), resolvedName);
		// Inject flags
		int flags = handle.getModifiers() & Modifier.RECOGNIZED_FIELD_MODIFIERS;
		flags |= IS_FIELD | (refKind << MN_REFERENCE_KIND_SHIFT);
		ops.putInt(memberName, "flags", flags);
	}

	private ObjectValue forInvokeDynamicCall(InstanceClass caller, Object arg) {
		VMOperations ops = this.ops;
		if (arg instanceof Long) {
			return ops.boxLong((long) arg);
		}
		if (arg instanceof Double) {
			return ops.boxDouble((double) arg);
		}
		if (arg instanceof Integer) {
			return ops.boxInt((int) arg);
		}
		if (arg instanceof Float) {
			return ops.boxFloat((float) arg);
		}
		if (arg instanceof String) {
			return stringPool.intern((String) arg);
		}
		if (arg instanceof Type) {
			Type type = (Type) arg;
			ObjectValue classLoader = caller.getClassLoader();
			if (type.getSort() == Type.METHOD) {
				// TODO fix me
				return ops.methodType(classLoader, type);
			} else {
				return ops.findClass(caller, type, false).getOop();
			}
		}
		if (arg instanceof Handle) {
			return ops.linkMethodHandleConstant(caller, (Handle) arg);
		}
		if (arg instanceof ConstantDynamic) {
			return ops.linkDynamic((ConstantDynamic) arg, caller);
		}
		throw new UnsupportedOperationException(Objects.toString(arg));
	}
}
