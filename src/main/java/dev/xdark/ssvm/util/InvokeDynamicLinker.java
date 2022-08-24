package dev.xdark.ssvm.util;

import dev.xdark.ssvm.LinkResolver;
import dev.xdark.ssvm.NativeJava;
import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.asm.Modifier;
import dev.xdark.ssvm.execution.Locals;
import dev.xdark.ssvm.execution.Stack;
import dev.xdark.ssvm.memory.management.MemoryManager;
import dev.xdark.ssvm.memory.management.StringPool;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.mirror.JavaField;
import dev.xdark.ssvm.mirror.JavaMethod;
import dev.xdark.ssvm.symbol.VMSymbols;
import dev.xdark.ssvm.thread.ThreadStorage;
import dev.xdark.ssvm.value.ArrayValue;
import dev.xdark.ssvm.value.InstanceValue;
import dev.xdark.ssvm.value.JavaValue;
import dev.xdark.ssvm.value.ObjectValue;
import dev.xdark.ssvm.value.sink.ValueSink;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;

import java.util.List;

import static dev.xdark.ssvm.asm.Modifier.ACC_VM_HIDDEN;
import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ACC_STATIC;

/**
 * InvokeDynamic linkage logic.
 *
 * @author xDark
 */
public final class InvokeDynamicLinker {

	public static final int MN_IS_METHOD = 0x00010000,
		MN_IS_CONSTRUCTOR = 0x00020000,
		MN_IS_FIELD = 0x00040000,
		MN_IS_TYPE = 0x00080000,
		MN_CALLER_SENSITIVE = 0x00100000,
		MN_REFERENCE_KIND_SHIFT = 24,
		MN_REFERENCE_KIND_MASK = 0x0F000000 >> MN_REFERENCE_KIND_SHIFT,
		MN_SEARCH_SUPERCLASSES = 0x00100000,
		MN_SEARCH_INTERFACES = 0x00200000;
	public static final byte
		REF_getField = 1,
		REF_getStatic = 2,
		REF_putField = 3,
		REF_putStatic = 4,
		REF_invokeVirtual = 5,
		REF_invokeStatic = 6,
		REF_invokeSpecial = 7,
		REF_newInvokeSpecial = 8,
		REF_invokeInterface = 9;
	public static final int
		IS_METHOD = MN_IS_METHOD,
		IS_CONSTRUCTOR = MN_IS_CONSTRUCTOR,
		IS_FIELD = MN_IS_FIELD,
		IS_TYPE = MN_IS_TYPE;
	public static final int ALL_KINDS = IS_METHOD | IS_CONSTRUCTOR | IS_FIELD | IS_TYPE;

	private final VirtualMachine vm;

	/**
	 * @param vm VM instance.
	 */
	public InvokeDynamicLinker(VirtualMachine vm) {
		this.vm = vm;
	}

	/**
	 * Links {@link InvokeDynamicInsnNode}.
	 *
	 * @param insn   Node to link.
	 * @param caller Method caller.
	 * @return Linked method handle or call site.
	 */
	public InstanceValue linkCall(InvokeDynamicInsnNode insn, InstanceJavaClass caller) {
		VirtualMachine vm = this.vm;
		VMHelper helper = vm.getHelper();
		VMSymbols symbols = vm.getSymbols();
		Handle bootstrap = insn.bsm;
		if (bootstrap.getTag() != Opcodes.H_INVOKESTATIC) {
			helper.throwException(symbols.java_lang_IllegalStateException(), "Bootstrap tag is not static");
		}
		InstanceValue linker = helper.linkMethodHandleConstant(caller, bootstrap);

		Object[] $bsmArgs = insn.bsmArgs;
		ArrayValue bsmArgs = helper.newArray(symbols.java_lang_Object(), $bsmArgs.length);
		for (int i = 0; i < $bsmArgs.length; i++) {
			bsmArgs.setReference(i, forInvokeDynamicCall($bsmArgs[i]));
		}

		StringPool stringPool = vm.getStringPool();
		ArrayValue appendix = helper.newArray(symbols.java_lang_Object(), 1);
		InstanceJavaClass natives = symbols.java_lang_invoke_MethodHandleNatives();
		JavaMethod method = natives.getStaticMethod("linkCallSite", "(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/invoke/MemberName;");
		Locals linkArgs;
		if (method == null) {
			// Bogus cp index entry which was removed
			// shortly after it was added, shaking
			method = natives.getStaticMethod("linkCallSite", "(Ljava/lang/Object;ILjava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/invoke/MemberName;");
			linkArgs = vm.getThreadStorage().newLocals(method);
			linkArgs.setReference(0, caller.getOop());
			linkArgs.setInt(1, 0);
			linkArgs.setReference(2, linker);
			linkArgs.setReference(3, stringPool.intern(insn.name));
			linkArgs.setReference(4, helper.methodType(caller.getClassLoader(), Type.getMethodType(insn.desc)));
			linkArgs.setReference(5, bsmArgs);
			linkArgs.setReference(6, appendix);
		} else {
			linkArgs = vm.getThreadStorage().newLocals(method);
			linkArgs.setReference(0, caller.getOop());
			linkArgs.setReference(1, linker);
			linkArgs.setReference(2, stringPool.intern(insn.name));
			linkArgs.setReference(3, helper.methodType(caller.getClassLoader(), Type.getMethodType(insn.desc)));
			linkArgs.setReference(4, bsmArgs);
			linkArgs.setReference(5, appendix);
		}

		helper.invoke(method, linkArgs);
		return (InstanceValue) appendix.getValue(0);
	}

	/**
	 * Invokes linked dynamic call.
	 *
	 * @param stack   Stack to sink arguments from.
	 * @param desc   Call descriptor.
	 * @param handle Call site or method handle.
	 * @param sink Result sink.
	 */
	public void dynamicCall(Stack stack, String desc, InstanceValue handle, ValueSink sink) {
		VirtualMachine vm = this.vm;
		VMHelper helper = vm.getHelper();
		LinkResolver linkResolver = vm.getPublicLinkResolver();
		ThreadStorage ts = vm.getThreadStorage();
		if (vm.getSymbols().java_lang_invoke_CallSite().isAssignableFrom(handle.getJavaClass())) {
			// See linkCallSiteImpl
			JavaMethod getTarget = linkResolver.resolveVirtualMethod(handle, "getTarget", "()Ljava/lang/invoke/MethodHandle;");
			Locals locals = ts.newLocals(getTarget);
			locals.setReference(0, handle);
			handle = helper.checkNotNull(helper.invokeReference(getTarget, locals));
		}
		JavaMethod invokeExact = linkResolver.resolveVirtualMethod(handle, "invokeExact", desc);
		Locals locals = ts.newLocals(invokeExact);
		locals.setReference(0, handle);
		stack.sinkInto(locals, 1, invokeExact.getMaxArgs() - 1);
		helper.invoke(invokeExact, locals, sink);
	}

	/**
	 * Sets up method handles implementation.
	 */
	public void setupMethodHandles() {
		VMSymbols symbols = vm.getSymbols();
		inject:
		{
			InstanceJavaClass memberName = symbols.java_lang_invoke_MemberName();
			List<FieldNode> fields = memberName.getNode().fields;
			fields.add(new FieldNode(
				ACC_PRIVATE | ACC_VM_HIDDEN,
				NativeJava.VM_INDEX,
				"I",
				null,
				null
			));
			for (int i = 0; i < fields.size(); i++) {
				FieldNode fn = fields.get(i);
				if ("method".equals(fn.name) && "Ljava/lang/invoke/ResolvedMethodName;".equals(fn.desc)) {
					break inject;
				}
			}
			fields.add(new FieldNode(
				ACC_PRIVATE | ACC_VM_HIDDEN,
				"method",
				"Ljava/lang/invoke/ResolvedMethodName;",
				null,
				null
			));
		}

		{
			InstanceJavaClass resolvedMethodName = symbols.java_lang_invoke_ResolvedMethodName();
			List<FieldNode> fields = resolvedMethodName.getNode().fields;
			fields.add(new FieldNode(
				ACC_PRIVATE | ACC_VM_HIDDEN,
				NativeJava.VM_TARGET,
				"Ljava/lang/Object;",
				null,
				null
			));
			fields.add(new FieldNode(
				ACC_PRIVATE | ACC_VM_HIDDEN,
				NativeJava.VM_HOLDER,
				"Ljava/lang/Object;",
				null,
				null
			));
		}
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
		VirtualMachine vm = this.vm;
		VMSymbols symbols = vm.getSymbols();
		VMOperations ops = vm.getTrustedOperations();
		// Inject vmholder & vmtarget into resolved name
		ops.putInt(memberName, NativeJava.VM_INDEX, handle.getSlot());
		MemoryManager memoryManager = vm.getMemoryManager();
		InstanceJavaClass rmn = symbols.java_lang_invoke_ResolvedMethodName();
		rmn.initialize();
		InstanceValue resolvedName = memoryManager.newInstance(rmn);
		resolvedName.initialize();
		ops.putReference(resolvedName, NativeJava.VM_TARGET, "Ljava/lang/Object;", vm.getHelper().boxInt(handle.getSlot()));
		ops.putReference(resolvedName, NativeJava.VM_HOLDER, "Ljava/lang/Object;", handle.getOwner().getOop());
		ops.putReference(memberName, "method", symbols.java_lang_invoke_ResolvedMethodName().getDescriptor(), resolvedName);
		// Inject flags
		int flags = handle.getAccess() & Modifier.RECOGNIZED_METHOD_MODIFIERS;
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
		VirtualMachine vm = this.vm;
		VMSymbols symbols = vm.getSymbols();
		VMOperations ops = vm.getTrustedOperations();
		// Inject vmholder & vmtarget into resolved name
		MemoryManager memoryManager = vm.getMemoryManager();
		InstanceJavaClass owner = handle.getOwner();
		long offset = handle.getOffset();
		if ((handle.getAccess() & ACC_STATIC) == 0) {
			offset += memoryManager.valueBaseOffset(owner);
		} else {
			offset += memoryManager.getStaticOffset(owner);
		}
		ops.putInt(memberName, NativeJava.VM_INDEX, (int) offset);
		InstanceJavaClass rmn = symbols.java_lang_invoke_ResolvedMethodName();
		rmn.initialize();
		InstanceValue resolvedName = memoryManager.newInstance(rmn);
		resolvedName.initialize();
		ops.putReference(resolvedName, NativeJava.VM_TARGET, "Ljava/lang/Object;", vm.getHelper().boxInt(handle.getSlot()));
		ops.putReference(resolvedName, NativeJava.VM_HOLDER, "Ljava/lang/Object;", owner.getOop());
		ops.putReference(memberName, "method", symbols.java_lang_invoke_ResolvedMethodName().getDescriptor(), resolvedName);
		// Inject flags
		int flags = handle.getAccess() & Modifier.RECOGNIZED_FIELD_MODIFIERS;
		flags |= IS_FIELD | (refKind << MN_REFERENCE_KIND_SHIFT);
		ops.putInt(memberName, "flags", flags);
	}

	/**
	 * Reads method handle target.
	 *
	 * @param handle Handle to read target from.
	 * @return Method handle target.
	 * Throws VM exception if handle is not initialized.
	 */
	public JavaMethod readVMTargetFromHandle(InstanceValue handle) {
		VirtualMachine vm = this.vm;
		VMHelper helper = vm.getHelper();
		VMOperations ops = vm.getTrustedOperations();
		InstanceValue form = helper.checkNotNull(ops.getReference(handle, "form", "Ljava/lang/invoke/LambdaForm;"));
		InstanceValue vmentry = helper.checkNotNull(ops.getReference(form, "vmentry", "Ljava/lang/invoke/MemberName;"));
		return readVMTargetFromMemberName(vmentry);
	}

	/**
	 * Reads method handle target.
	 *
	 * @param vmentry Member name to read target from.
	 * @return Method handle target.
	 * Throws VM exception if handle is not initialized.
	 */
	public JavaMethod readVMTargetFromMemberName(InstanceValue vmentry) {
		VirtualMachine vm = this.vm;
		VMHelper helper = vm.getHelper();
		VMOperations ops = vm.getTrustedOperations();
		InstanceValue resolved = helper.checkNotNull(ops.getReference(vmentry, "method", vm.getSymbols().java_lang_invoke_ResolvedMethodName().getDescriptor()));
		InstanceJavaClass clazz = ((JavaValue<InstanceJavaClass>) ops.getReference(vmentry, "clazz", "Ljava/lang/Class;")).getValue();
		return helper.getMethodBySlot(clazz, ops.getInt(ops.getReference(resolved, NativeJava.VM_TARGET, "Ljava/lang/Object;"), "value"));
	}

	private ObjectValue forInvokeDynamicCall(Object cst) {
		VMHelper helper = vm.getHelper();
		if (cst instanceof Long) {
			return helper.boxLong((Long) cst);
		}
		if (cst instanceof Double) {
			return helper.boxDouble((Double) cst);
		}
		if (cst instanceof Integer || cst instanceof Short || cst instanceof Byte) {
			return helper.boxInt(((Number) cst).intValue());
		}
		if (cst instanceof Character) {
			return helper.boxInt((Character) cst);
		}
		if (cst instanceof Float) {
			return helper.boxFloat((Float) cst);
		}
		if (cst instanceof Boolean) {
			return helper.boxBoolean((Boolean) cst);
		}
		return (ObjectValue) helper.referenceFromLdc(cst);
	}
}
