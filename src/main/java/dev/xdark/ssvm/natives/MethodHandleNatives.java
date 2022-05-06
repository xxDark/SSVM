package dev.xdark.ssvm.natives;

import dev.xdark.ssvm.NativeJava;
import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.api.MethodInvoker;
import dev.xdark.ssvm.api.VMInterface;
import dev.xdark.ssvm.asm.Modifier;
import dev.xdark.ssvm.execution.Locals;
import dev.xdark.ssvm.execution.PanicException;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.memory.MemoryManager;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.mirror.JavaClass;
import dev.xdark.ssvm.mirror.JavaField;
import dev.xdark.ssvm.mirror.JavaMethod;
import dev.xdark.ssvm.util.VMHelper;
import dev.xdark.ssvm.symbol.VMSymbols;
import dev.xdark.ssvm.value.*;
import lombok.experimental.UtilityClass;
import org.objectweb.asm.Type;

import java.util.Arrays;

import static org.objectweb.asm.Opcodes.*;

/**
 * Initializes JSR 292 related components.
 *
 * @author xDark
 */
@UtilityClass
public class MethodHandleNatives {

	private final String VM_INDEX = NativeJava.VM_INDEX;
	private final String VM_TARGET = NativeJava.VM_TARGET;
	private final String VM_HOLDER = NativeJava.VM_HOLDER;

	private final int MN_IS_METHOD = 0x00010000,
			MN_IS_CONSTRUCTOR = 0x00020000,
			MN_IS_FIELD = 0x00040000,
			MN_IS_TYPE = 0x00080000,
			MN_CALLER_SENSITIVE = 0x00100000,
			MN_REFERENCE_KIND_SHIFT = 24,
			MN_REFERENCE_KIND_MASK = 0x0F000000 >> MN_REFERENCE_KIND_SHIFT,
			MN_SEARCH_SUPERCLASSES = 0x00100000,
			MN_SEARCH_INTERFACES = 0x00200000;
	private final byte
			REF_getField = 1,
			REF_getStatic = 2,
			REF_putField = 3,
			REF_putStatic = 4,
			REF_invokeVirtual = 5,
			REF_invokeStatic = 6,
			REF_invokeSpecial = 7,
			REF_newInvokeSpecial = 8,
			REF_invokeInterface = 9;
	private final int
			IS_METHOD = MN_IS_METHOD,
			IS_CONSTRUCTOR = MN_IS_CONSTRUCTOR,
			IS_FIELD = MN_IS_FIELD,
			IS_TYPE = MN_IS_TYPE;
	static final int ALL_KINDS = IS_METHOD | IS_CONSTRUCTOR | IS_FIELD | IS_TYPE;

	/**
	 * @param vm
	 * 		VM instance.
	 */
	public void init(VirtualMachine vm) {
		VMInterface vmi = vm.getInterface();
		VMSymbols symbols = vm.getSymbols();
		InstanceJavaClass natives = symbols.java_lang_invoke_MethodHandleNatives();
		vmi.setInvoker(natives, "registerNatives", "()V", MethodInvoker.noop());
		byte[] speculativeResolve = new byte[]{-1};
		MethodInvoker resolve = ctx -> {
			VMHelper helper = vm.getHelper();
			Locals locals = ctx.getLocals();
			InstanceValue memberName = helper.checkNotNull(locals.load(0));
			resolveMemberName(speculativeResolve[0], locals, memberName);
			ctx.setResult(memberName);
			return Result.ABORT;
		};
		speculativeResolve[0] = 3;
		if (!vmi.setInvoker(natives, "resolve", "(Ljava/lang/invoke/MemberName;Ljava/lang/Class;IZ)Ljava/lang/invoke/MemberName;", resolve)) {
			speculativeResolve[0] = 2;
			if (!vmi.setInvoker(natives, "resolve", "(Ljava/lang/invoke/MemberName;Ljava/lang/Class;Z)Ljava/lang/invoke/MemberName;", resolve)) {
				speculativeResolve[0] = -1;
				if (!vmi.setInvoker(natives, "resolve", "(Ljava/lang/invoke/MemberName;Ljava/lang/Class;)Ljava/lang/invoke/MemberName;", resolve)) {
					throw new IllegalStateException("Unable to locate MethodHandleNatives#resolve method");
				}
			}
		}
		vmi.setInvoker(natives, "getConstant", "(I)I", ctx -> {
			ctx.setResult(IntValue.ZERO);
			return Result.ABORT;
		});
		vmi.setInvoker(natives, "init", "(Ljava/lang/invoke/MemberName;Ljava/lang/Object;)V", ctx -> {
			Locals locals = ctx.getLocals();
			VMHelper helper = ctx.getHelper();
			InstanceValue memberName = helper.checkNotNull(locals.load(0));
			InstanceValue obj = helper.checkNotNull(locals.load(1));
			InstanceJavaClass objClass = obj.getJavaClass();
			if (objClass == symbols.java_lang_reflect_Method()) {
				initMemberNameMethod(vm, memberName, obj);
			} else if (objClass == symbols.java_lang_reflect_Field()) {
				initMemberNameField(vm, memberName, obj);
			} else if (objClass == symbols.java_lang_reflect_Constructor()) {
				initMemberNameConstructor(vm, memberName, obj);
			} else {
				helper.throwException(symbols.java_lang_InternalError(), "Unsupported class: " + objClass.getName());
			}
			return Result.ABORT;
		});
		vmi.setInvoker(natives, "objectFieldOffset", "(Ljava/lang/invoke/MemberName;)J", ctx -> {
			ctx.setResult(LongValue.of(ctx.getLocals().<InstanceValue>load(0).getInt(VM_INDEX)));
			return Result.ABORT;
		});
		vmi.setInvoker(natives, "staticFieldBase", "(Ljava/lang/invoke/MemberName;)Ljava/lang/Object;", ctx -> {
			ctx.setResult(ctx.getLocals().<InstanceValue>load(0).getValue("clazz", "Ljava/lang/Class;"));
			return Result.ABORT;
		});
		vmi.setInvoker(natives, "staticFieldOffset", "(Ljava/lang/invoke/MemberName;)J", ctx -> {
			ctx.setResult(LongValue.of(ctx.getLocals().<InstanceValue>load(0).getInt(VM_INDEX)));
			return Result.ABORT;
		});
		vmi.setInvoker(natives, "getMembers", "(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/String;ILjava/lang/Class;I[Ljava/lang/invoke/MemberName;)I", ctx -> {
			ctx.setResult(IntValue.ZERO);
			return Result.ABORT;
		});

		MethodInvoker setCallSiteTarget = ctx -> {
			Locals locals = ctx.getLocals();
			locals.<InstanceValue>load(0).setValue("target", "Ljava/lang/invoke/MethodHandle;", locals.load(1));
			return Result.ABORT;
		};
		vmi.setInvoker(natives, "setCallSiteTargetNormal", "(Ljava/lang/invoke/CallSite;Ljava/lang/invoke/MethodHandle;)V", setCallSiteTarget);
		vmi.setInvoker(natives, "setCallSiteTargetVolatile", "(Ljava/lang/invoke/CallSite;Ljava/lang/invoke/MethodHandle;)V", setCallSiteTarget);

		InstanceJavaClass mh = symbols.java_lang_invoke_MethodHandle();
		MethodInvoker invoke = ctx -> {
			Locals locals = ctx.getLocals();
			VMHelper helper = vm.getHelper();
			InstanceValue _this = locals.load(0);
			InstanceValue form = helper.checkNotNull(_this.getValue("form", "Ljava/lang/invoke/LambdaForm;"));
			InstanceValue vmentry = helper.checkNotNull(form.getValue("vmentry", "Ljava/lang/invoke/MemberName;"));
			InstanceValue resolved = helper.checkNotNull(vmentry.getValue("method", symbols.java_lang_invoke_ResolvedMethodName().getDescriptor()));
			Object vmtarget = ((JavaValue<Object>) resolved.getValue(VM_TARGET, "Ljava/lang/Object;")).getValue();
			if (vmtarget instanceof JavaMethod) {
				JavaMethod jm = (JavaMethod) vmtarget;
				String name = jm.getName();
				if ("<init>".equals(name)) {
					helper.throwException(symbols.java_lang_InternalError(), "Bad name " + name);
				}
				Value[] lvt = locals.getTable();

				InstanceJavaClass owner = jm.getOwner();
				Value result;
				if ((jm.getAccess() & ACC_STATIC) == 0) {
					int flags = vmentry.getInt("flags");
					int refKind = (flags >> MN_REFERENCE_KIND_SHIFT) & MN_REFERENCE_KIND_MASK;
					if (refKind == REF_invokeSpecial || refKind == REF_newInvokeSpecial) {
						result = helper.invokeExact(owner, jm, new Value[0], lvt).getResult();
					} else {
						result = helper.invokeVirtual(name, jm.getDesc(), new Value[0], lvt).getResult();
					}
				} else {
					result = helper.invokeStatic(owner, jm, new Value[0], lvt).getResult();
				}
				JavaMethod m = ctx.getMethod();
				result = Util.convertInvokeDynamicArgument(helper, m.getReturnType(), result);
				ctx.setResult(voidAsNull(result, m));
			} else {
				throw new PanicException("TODO: " + vmtarget);
			}
			return Result.ABORT;
		};
		vmi.setInvoker(mh, "invoke", "([Ljava/lang/Object;)Ljava/lang/Object;", invoke);
		vmi.setInvoker(mh, "invokeBasic", "([Ljava/lang/Object;)Ljava/lang/Object;", invoke);
		vmi.setInvoker(mh, "invokeExact", "([Ljava/lang/Object;)Ljava/lang/Object;", invoke);

		MethodInvoker linkToXX = ctx -> {
			Locals locals = ctx.getLocals();
			VMHelper helper = vm.getHelper();
			Value[] table = locals.getTable();
			int length = table.length;
			InstanceValue memberName = locals.load(length - 1);
			InstanceValue resolved = (InstanceValue) memberName.getValue("method", symbols.java_lang_invoke_ResolvedMethodName().getDescriptor());
			JavaMethod vmtarget = ((JavaValue<JavaMethod>) resolved.getValue(VM_TARGET, "Ljava/lang/Object;")).getValue();

			Value[] args = Arrays.copyOfRange(table, 0, length - 1);

			Value result;
			if ((vmtarget.getAccess() & ACC_STATIC) == 0) {
				int flags = memberName.getInt("flags");
				int refKind = (flags >> MN_REFERENCE_KIND_SHIFT) & MN_REFERENCE_KIND_MASK;
				if (refKind == REF_invokeSpecial) {
					result = helper.invokeExact(vmtarget.getOwner(), vmtarget, new Value[0], args).getResult();
				} else {
					result = helper.invokeVirtual(vmtarget.getName(), vmtarget.getDesc(), new Value[0], args).getResult();
				}
			} else {
				result = helper.invokeStatic(vmtarget.getOwner(), vmtarget, new Value[0], args).getResult();
			}
			JavaMethod m = ctx.getMethod();
			result = Util.convertInvokeDynamicArgument(helper, m.getReturnType(), result);
			ctx.setResult(voidAsNull(result, m));
			return Result.ABORT;
		};

		vmi.setInvoker(mh, "linkToStatic", "([Ljava/lang/Object;)Ljava/lang/Object;", linkToXX);
		vmi.setInvoker(mh, "linkToVirtual", "([Ljava/lang/Object;)Ljava/lang/Object;", linkToXX);
		vmi.setInvoker(mh, "linkToInterface", "([Ljava/lang/Object;)Ljava/lang/Object;", linkToXX);
		vmi.setInvoker(mh, "linkToSpecial", "([Ljava/lang/Object;)Ljava/lang/Object;", linkToXX);

		InstanceJavaClass lookup = symbols.java_lang_invoke_MethodHandles$Lookup();
		vmi.setInvoker(lookup, "checkAccess", "(BLjava/lang/Class;Ljava/lang/invoke/MemberName;)V", MethodInvoker.noop());

		// TODO impl getMemberVMInfo
		InstanceJavaClass memberName = symbols.java_lang_invoke_MemberName();
		vmi.setInvoker(memberName, "vminfoIsConsistent", "()Z", ctx -> {
			ctx.setResult(IntValue.ONE);
			return Result.ABORT;
		});
	}

	private void resolveMemberName(byte speculativeResolveModeIndex, Locals locals, InstanceValue memberName) {
		JavaValue<InstanceJavaClass> classWrapper = (JavaValue<InstanceJavaClass>) memberName.getValue("clazz", "Ljava/lang/Class;");
		InstanceJavaClass clazz = classWrapper.getValue();
		clazz.initialize();
		VirtualMachine vm = clazz.getVM();
		VMHelper helper = vm.getHelper();
		String name = helper.readUtf8(memberName.getValue("name", "Ljava/lang/String;"));
		ObjectValue mt = memberName.getValue("type", "Ljava/lang/Object;");
		int flags = memberName.getInt("flags");
		int refKind = (flags >> MN_REFERENCE_KIND_SHIFT) & MN_REFERENCE_KIND_MASK;
		boolean speculativeResolve0 = speculativeResolveModeIndex >= 0 && locals.load(speculativeResolveModeIndex).asBoolean();
		switch(flags & ALL_KINDS) {
			case IS_METHOD:
				initMethodMember(refKind, vm, memberName, clazz, name, mt, IS_METHOD, speculativeResolve0);
				break;
			case IS_FIELD:
				initFieldMember(refKind, vm, memberName, clazz, name, mt, speculativeResolve0);
				break;
			case IS_CONSTRUCTOR:
				initMethodMember(refKind, vm, memberName, clazz, name, mt, IS_CONSTRUCTOR, speculativeResolve0);
				break;
			default:
				helper.throwException(vm.getSymbols().java_lang_InternalError(), "Not implemented for " + refKind + " " + (flags & ALL_KINDS));
		}
	}

	private void initMemberNameMethod(VirtualMachine vm, InstanceValue memberName, InstanceValue obj) {
		VMHelper helper = vm.getHelper();
		// Copy over clazz, name, type & invoke expand
		InstanceJavaClass clazz = ((JavaValue<InstanceJavaClass>) obj.getValue("clazz", "Ljava/lang/Class;")).getValue();
		int slot = obj.getInt("slot");
		JavaMethod method = helper.getMethodBySlot(clazz, slot);

		memberName.setValue("clazz", "Ljava/lang/Class;", clazz.getOop());
		memberName.setValue("name", "Ljava/lang/String;", vm.getStringPool().intern(method.getName()));
		InstanceValue mt = helper.methodType(clazz.getClassLoader(), method.getType());
		memberName.setValue("type", "Ljava/lang/Object;", mt);
		int refKind;
		if ((method.getAccess() & ACC_STATIC) == 0) {
			refKind = REF_invokeVirtual;
		} else {
			refKind = REF_invokeStatic;
		}
		initMethodMember(refKind, vm, memberName, method, IS_METHOD);
	}

	private void initMemberNameConstructor(VirtualMachine vm, InstanceValue memberName, InstanceValue obj) {
		VMHelper helper = vm.getHelper();
		// Copy over clazz, name, type & invoke expand
		InstanceJavaClass clazz = ((JavaValue<InstanceJavaClass>) obj.getValue("clazz", "Ljava/lang/Class;")).getValue();
		int slot = obj.getInt("slot");
		JavaMethod method = helper.getMethodBySlot(clazz, slot);

		memberName.setValue("clazz", "Ljava/lang/Class;", clazz.getOop());
		memberName.setValue("name", "Ljava/lang/String;", vm.getStringPool().intern(method.getName()));
		InstanceValue mt = helper.methodType(clazz.getClassLoader(), method.getType());
		memberName.setValue("type", "Ljava/lang/Object;", mt);
		initMethodMember(REF_newInvokeSpecial, vm, memberName, method, IS_CONSTRUCTOR);
	}

	private void initMemberNameField(VirtualMachine vm, InstanceValue memberName, InstanceValue obj) {
		VMHelper helper = vm.getHelper();
		// Copy over clazz, name, type & invoke expand
		InstanceJavaClass clazz = ((JavaValue<InstanceJavaClass>) obj.getValue("clazz", "Ljava/lang/Class;")).getValue();
		int slot = obj.getInt("slot");
		JavaField field = helper.getFieldBySlot(clazz, slot);
		memberName.setValue("clazz", "Ljava/lang/Class;", clazz.getOop());
		memberName.setValue("name", "Ljava/lang/String;", vm.getStringPool().intern(field.getName()));
		InstanceValue mt = helper.findClass(clazz.getClassLoader(), field.getType().getInternalName(), false).getOop();
		memberName.setValue("type", "Ljava/lang/Object;", mt);
		int refKind;
		if ((field.getAccess() & ACC_STATIC) == 0) {
			refKind = REF_getField;
		} else {
			refKind = REF_getStatic;
		} // JDK code will change that later on.
		initFieldMember(refKind, vm, memberName, field);
	}

	private void initMethodMember(int refKind, VirtualMachine vm, InstanceValue memberName, JavaMethod handle, int mnType) {
		VMSymbols symbols = vm.getSymbols();
		// Inject vmholder & vmtarget into resolved name
		memberName.setInt(VM_INDEX, handle.getSlot());
		MemoryManager memoryManager = vm.getMemoryManager();
		InstanceJavaClass rmn = symbols.java_lang_invoke_ResolvedMethodName();
		rmn.initialize();
		InstanceValue resolvedName = memoryManager.newInstance(rmn);
		resolvedName.initialize();
		InstanceJavaClass jlo = symbols.java_lang_Object();
		resolvedName.setValue(VM_TARGET, "Ljava/lang/Object;", memoryManager.newJavaInstance(jlo, handle));
		resolvedName.setValue(VM_HOLDER, "Ljava/lang/Object;", handle.getOwner().getOop());
		memberName.setValue("method", symbols.java_lang_invoke_ResolvedMethodName().getDescriptor(), resolvedName);
		// Inject flags
		int flags = handle.getAccess() & Modifier.RECOGNIZED_METHOD_MODIFIERS;
		flags |= mnType | (refKind << MN_REFERENCE_KIND_SHIFT);
		memberName.setInt("flags", flags);
	}

	private void initFieldMember(int refKind, VirtualMachine vm, InstanceValue memberName, JavaField handle) {
		VMSymbols symbols = vm.getSymbols();
		// Inject vmholder & vmtarget into resolved name
		MemoryManager memoryManager = vm.getMemoryManager();
		InstanceJavaClass owner = handle.getOwner();
		long offset = handle.getOffset();
		if ((handle.getAccess() & ACC_STATIC) == 0) {
			offset += memoryManager.valueBaseOffset(owner);
		} else {
			offset += memoryManager.getStaticOffset(owner);
		}
		memberName.setInt(VM_INDEX, (int) offset);
		InstanceJavaClass rmn = symbols.java_lang_invoke_ResolvedMethodName();
		rmn.initialize();
		InstanceValue resolvedName = memoryManager.newInstance(rmn);
		resolvedName.initialize();
		InstanceJavaClass jlo = symbols.java_lang_Object();
		resolvedName.setValue(VM_TARGET, "Ljava/lang/Object;", memoryManager.newJavaInstance(jlo, handle));
		resolvedName.setValue(VM_HOLDER, "Ljava/lang/Object;", owner.getOop());
		memberName.setValue("method", symbols.java_lang_invoke_ResolvedMethodName().getDescriptor(), resolvedName);
		// Inject flags
		int flags = handle.getAccess() & Modifier.RECOGNIZED_FIELD_MODIFIERS;
		flags |= IS_FIELD | (refKind << MN_REFERENCE_KIND_SHIFT);
		memberName.setInt("flags", flags);
	}

	private void initMethodMember(int refKind, VirtualMachine vm, InstanceValue memberName, InstanceJavaClass clazz, String name, Value methodType, int type, boolean speculativeResolve0) {
		VMHelper helper = vm.getHelper();
		VMSymbols symbols = vm.getSymbols();
		String desc = helper.readUtf8(helper.invokeExact(symbols.java_lang_invoke_MethodType(), "toMethodDescriptorString", "()Ljava/lang/String;", new Value[0], new Value[]{
				methodType
		}).getResult());
		JavaMethod handle;
		switch(refKind) {
			case REF_invokeStatic:
				handle = clazz.getStaticMethod(name, desc);
				break;
			case REF_invokeSpecial:
			case REF_newInvokeSpecial:
				handle = clazz.getVirtualMethod(name, desc);
				break;
			case REF_invokeVirtual:
			case REF_invokeInterface:
				handle = clazz.getVirtualMethod(name, desc);
				if (handle == null) {
					handle = clazz.getInterfaceMethodRecursively(name, desc);
				}
				break;
			default:
				helper.throwException(symbols.java_lang_InternalError(), "unrecognized MemberName format");
				return;
		}
		if (handle == null) {
			if (!speculativeResolve0) {
				return;
			}
			helper.throwException(symbols.java_lang_NoSuchMethodError(), clazz.getInternalName() + '.' + name + desc);
		}
		initMethodMember(refKind, vm, memberName, handle, type);
	}

	private void initFieldMember(int refKind, VirtualMachine vm, InstanceValue memberName, InstanceJavaClass clazz, String name, Value type, boolean speculativeResolve0) {
		VMHelper helper = vm.getHelper();
		VMSymbols symbols = vm.getSymbols();
		String desc = ((JavaValue<JavaClass>) type).getValue().getDescriptor();

		// TODO hotspot "feature"?
		// https://github.com/openjdk/jdk/blob/026b85303c01326bc49a1105a89853d7641fcd50/src/hotspot/share/prims/methodHandles.cpp#L839
		// https://github.com/openjdk/jdk/blob/534e557874274255c55086b4f6128063cbd9cc58/src/hotspot/share/interpreter/linkResolver.cpp#L974
		JavaField handle;
		switch(refKind) {
			case REF_getStatic:
			case REF_putStatic:
			case REF_getField:
			case REF_putField:
				handle = clazz.getVirtualFieldRecursively(name, desc);
				if (handle == null) {
					handle = clazz.getStaticFieldRecursively(name, desc);
				}
				break;
			default:
				helper.throwException(symbols.java_lang_InternalError(), "unrecognized MemberName format");
				return;
		}
		if (handle == null) {
			if (speculativeResolve0) {
				return;
			}
			helper.throwException(symbols.java_lang_NoSuchFieldError(), name);
		}
		initFieldMember(refKind, vm, memberName, handle);
	}

	private Value voidAsNull(Value v, JavaMethod jm) {
		Type rt = jm.getReturnType();
		boolean isVoid = rt == Type.VOID_TYPE;
		if (isVoid) {
			return VoidValue.INSTANCE;
		}
		if (v.isVoid()) {
			// Return null if return type is non-void
			return NullValue.INSTANCE;
		}
		return v;
	}
}
