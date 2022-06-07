package dev.xdark.ssvm.natives;

import dev.xdark.ssvm.NativeJava;
import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.api.MethodInvoker;
import dev.xdark.ssvm.api.VMInterface;
import dev.xdark.ssvm.execution.Locals;
import dev.xdark.ssvm.execution.Result;
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
import static dev.xdark.ssvm.util.InvokeDynamicLinker.*;

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
			JavaMethod vmtarget = vm.getInvokeDynamicLinker().readVMTarget(_this);
			String name = vmtarget.getName();
			if ("<init>".equals(name)) {
				helper.throwException(symbols.java_lang_InternalError(), "Bad name " + name);
			}
			Value[] lvt = locals.getTable();

			InstanceJavaClass owner = vmtarget.getOwner();
			Value result;
			if ((vmtarget.getAccess() & ACC_STATIC) == 0) {
				int flags = vmentry.getInt("flags");
				int refKind = (flags >> MN_REFERENCE_KIND_SHIFT) & MN_REFERENCE_KIND_MASK;
				if (refKind == REF_invokeSpecial || refKind == REF_newInvokeSpecial) {
					result = helper.invokeExact(vmtarget, new Value[0], lvt).getResult();
				} else {
					result = helper.invokeVirtual(name, vmtarget.getDesc(), new Value[0], lvt).getResult();
				}
			} else {
				result = helper.invokeStatic(vmtarget, new Value[0], lvt).getResult();
			}
			JavaMethod m = ctx.getMethod();
			result = Util.convertInvokeDynamicArgument(helper, m.getReturnType(), result);
			ctx.setResult(voidAsNull(result, m));
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
			InstanceJavaClass clazz = ((JavaValue<InstanceJavaClass>) memberName.getValue("clazz", "Ljava/lang/Class;")).getValue();
			JavaMethod vmtarget = helper.getMethodBySlot(clazz, ((InstanceValue) resolved.getValue(VM_TARGET, "Ljava/lang/Object;")).getInt("value"));

			Value[] args = Arrays.copyOfRange(table, 0, length - 1);

			Value result;
			if ((vmtarget.getAccess() & ACC_STATIC) == 0) {
				int flags = memberName.getInt("flags");
				int refKind = (flags >> MN_REFERENCE_KIND_SHIFT) & MN_REFERENCE_KIND_MASK;
				if (refKind == REF_invokeSpecial) {
					result = helper.invokeExact(vmtarget, new Value[0], args).getResult();
				} else {
					result = helper.invokeVirtual(vmtarget.getName(), vmtarget.getDesc(), new Value[0], args).getResult();
				}
			} else {
				result = helper.invokeStatic(vmtarget, new Value[0], args).getResult();
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
		vm.getInvokeDynamicLinker().initMethodMember(refKind, memberName, method, IS_METHOD);
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
		vm.getInvokeDynamicLinker().initMethodMember(REF_newInvokeSpecial, memberName, method, IS_CONSTRUCTOR);
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
		vm.getInvokeDynamicLinker().initFieldMember(refKind, memberName, field);
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
					//noinspection deprecation
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
		vm.getInvokeDynamicLinker().initMethodMember(refKind, memberName, handle, type);
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
		vm.getInvokeDynamicLinker().initFieldMember(refKind, memberName, handle);
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
