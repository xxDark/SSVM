package dev.xdark.ssvm.operation;

import dev.xdark.ssvm.LinkResolver;
import dev.xdark.ssvm.NativeJava;
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
	private final LinkResolver linkResolver;
	private final ClassStorage classStorage;
	private final MemoryManager memoryManager;
	private final ExceptionOperations exceptionOperations;
	private final AllocationOperations allocationOperations;
	private final MethodHandleOperations methodHandleOperations;
	private final InvocationOperations invocationOperations;
	private final VerificationOperations verificationOperations;
	private final FieldOperations fieldOperations;
	private final ClassOperations classOperations;
	private final PrimitiveOperations primitiveOperations;

	@Override
	public InstanceValue linkCall(InvokeDynamicInsnNode insn, InstanceClass caller) {
		Symbols symbols = this.symbols;
		Handle bootstrap = insn.bsm;
		if (bootstrap.getTag() != Opcodes.H_INVOKESTATIC) {
			exceptionOperations.throwException(symbols.java_lang_IllegalStateException(), "Bootstrap tag is not static");
		}
		InstanceValue linker = methodHandleOperations.linkMethodHandleConstant(caller, bootstrap);

		Object[] $bsmArgs = insn.bsmArgs;
		ArrayValue bsmArgs = allocationOperations.allocateArray(symbols.java_lang_Object(), $bsmArgs.length);
		for (int i = 0; i < $bsmArgs.length; i++) {
			bsmArgs.setReference(i, forInvokeDynamicCall(caller, $bsmArgs[i]));
		}

		StringPool stringPool = this.stringPool;
		ArrayValue appendix = allocationOperations.allocateArray(symbols.java_lang_Object(), 1);
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
			linkArgs.setReference(4, methodHandleOperations.methodType(caller.getClassLoader(), Type.getMethodType(insn.desc)));
			linkArgs.setReference(5, bsmArgs);
			linkArgs.setReference(6, appendix);
		} else {
			linkArgs = threadManager.currentOsThread().getStorage().newLocals(method);
			linkArgs.setReference(0, caller.getOop());
			linkArgs.setReference(1, linker);
			linkArgs.setReference(2, stringPool.intern(insn.name));
			linkArgs.setReference(3, methodHandleOperations.methodType(caller.getClassLoader(), Type.getMethodType(insn.desc)));
			linkArgs.setReference(4, bsmArgs);
			linkArgs.setReference(5, appendix);
		}

		invocationOperations.invokeVoid(method, linkArgs);
		return (InstanceValue) appendix.getReference(0);
	}

	@Override
	public void dynamicCall(Stack stack, String desc, InstanceValue handle, ValueSink sink) {
		ThreadStorage ts = threadManager.currentOsThread().getStorage();
		if (symbols.java_lang_invoke_CallSite().isAssignableFrom(handle.getJavaClass())) {
			// See linkCallSiteImpl
			JavaMethod getTarget = linkResolver.resolveVirtualMethod(handle, "getTarget", "()Ljava/lang/invoke/MethodHandle;");
			Locals locals = ts.newLocals(getTarget);
			locals.setReference(0, handle);
			handle = verificationOperations.checkNotNull(invocationOperations.invokeReference(getTarget, locals));
		}
		JavaMethod invokeExact = linkResolver.resolveVirtualMethod(handle, "invokeExact", desc);
		Locals locals = ts.newLocals(invokeExact);
		locals.setReference(0, handle);
		stack.sinkInto(locals, 1, invokeExact.getMaxArgs() - 1);
		invocationOperations.invoke(invokeExact, locals, sink);
	}

	@Override
	public JavaMethod readVMTargetFromHandle(InstanceValue handle) {
		VerificationOperations vops = verificationOperations;
		FieldOperations fops = fieldOperations;
		InstanceValue form = vops.checkNotNull(fops.getReference(handle, "form", "Ljava/lang/invoke/LambdaForm;"));
		InstanceValue vmentry = vops.checkNotNull(fops.getReference(form, "vmentry", "Ljava/lang/invoke/MemberName;"));
		return readVMTargetFromMemberName(vmentry);
	}

	@Override
	public JavaMethod readVMTargetFromMemberName(InstanceValue vmentry) {
		VerificationOperations vops = verificationOperations;
		FieldOperations fops = fieldOperations;
		InstanceValue resolved = vops.checkNotNull(fops.getReference(vmentry, "method", symbols.java_lang_invoke_ResolvedMethodName().getDescriptor()));
		InstanceClass clazz = (InstanceClass) classStorage.lookup(vops.checkNotNull(fops.getReference(vmentry, "clazz", "Ljava/lang/Class;")));
		return clazz.getMethodBySlot(fops.getInt(fops.getReference(resolved, InjectedClassLayout.java_lang_invoke_ResolvedMethodName_vmtarget.name(), InjectedClassLayout.java_lang_invoke_ResolvedMethodName_vmtarget.descriptor()), "value"));
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
		FieldOperations fops = fieldOperations;
		Symbols symbols = this.symbols;
		// Inject vmholder & vmtarget into resolved name
		fops.putInt(memberName, InjectedClassLayout.java_lang_invoke_MemberName_vmindex.name(), handle.getSlot());
		MemoryManager memoryManager = this.memoryManager;
		InstanceClass rmn = symbols.java_lang_invoke_ResolvedMethodName();
		classOperations.initialize(rmn);
		InstanceValue resolvedName = memoryManager.newInstance(rmn);
		fops.putReference(resolvedName, InjectedClassLayout.java_lang_invoke_ResolvedMethodName_vmtarget.name(), InjectedClassLayout.java_lang_invoke_ResolvedMethodName_vmtarget.descriptor(), primitiveOperations.boxInt(handle.getSlot()));
		fops.putReference(resolvedName, InjectedClassLayout.java_lang_invoke_ResolvedMethodName_vmholder.name(), InjectedClassLayout.java_lang_invoke_ResolvedMethodName_vmholder.descriptor(), handle.getOwner().getOop());
		fops.putReference(memberName, "method", symbols.java_lang_invoke_ResolvedMethodName().getDescriptor(), resolvedName);
		// Inject flags
		int flags = handle.getModifiers() & Modifier.RECOGNIZED_METHOD_MODIFIERS;
		flags |= mnType | (refKind << MN_REFERENCE_KIND_SHIFT);
		fops.putInt(memberName, "flags", flags);
	}

	/**
	 * Initializes field member.
	 *
	 * @param refKind    Reference kind.
	 * @param memberName Member name instance.
	 * @param handle     Field handle.
	 */
	public void initFieldMember(int refKind, InstanceValue memberName, JavaField handle) {
		FieldOperations fops = fieldOperations;
		// Inject vmholder & vmtarget into resolved name
		InstanceClass owner = handle.getOwner();
		long offset = handle.getOffset();
		fops.putInt(memberName, InjectedClassLayout.java_lang_invoke_MemberName_vmindex.name(), (int) offset);
		InstanceClass rmn = symbols.java_lang_invoke_ResolvedMethodName();
		classOperations.initialize(rmn);
		InstanceValue resolvedName = memoryManager.newInstance(rmn);
		fops.putReference(resolvedName, InjectedClassLayout.java_lang_invoke_ResolvedMethodName_vmtarget.name(), InjectedClassLayout.java_lang_invoke_ResolvedMethodName_vmtarget.descriptor(), primitiveOperations.boxInt(handle.getSlot()));
		fops.putReference(resolvedName, InjectedClassLayout.java_lang_invoke_ResolvedMethodName_vmholder.name(), InjectedClassLayout.java_lang_invoke_ResolvedMethodName_vmholder.descriptor(), owner.getOop());
		fops.putReference(memberName, "method", symbols.java_lang_invoke_ResolvedMethodName().getDescriptor(), resolvedName);
		// Inject flags
		int flags = handle.getModifiers() & Modifier.RECOGNIZED_FIELD_MODIFIERS;
		flags |= IS_FIELD | (refKind << MN_REFERENCE_KIND_SHIFT);
		fops.putInt(memberName, "flags", flags);
	}

	private ObjectValue forInvokeDynamicCall(InstanceClass caller, Object arg) {
		if (arg instanceof Long) {
			return primitiveOperations.boxLong((long) arg);
		}
		if (arg instanceof Double) {
			return primitiveOperations.boxDouble((double) arg);
		}
		if (arg instanceof Integer) {
			return primitiveOperations.boxInt((int) arg);
		}
		if (arg instanceof Float) {
			return primitiveOperations.boxFloat((float) arg);
		}
		if (arg instanceof String) {
			return stringPool.intern((String) arg);
		}
		if (arg instanceof Type) {
			Type type = (Type) arg;
			ObjectValue classLoader = caller.getClassLoader();
			if (type.getSort() == Type.METHOD) {
				return methodHandleOperations.methodType(classLoader, type);
			} else {
				return classOperations.findClass(classLoader, type, false).getOop();
			}
		}
		if (arg instanceof Handle) {
			return methodHandleOperations.linkMethodHandleConstant(caller, (Handle) arg);
		}
		throw new UnsupportedOperationException(Objects.toString(arg));
	}
}
