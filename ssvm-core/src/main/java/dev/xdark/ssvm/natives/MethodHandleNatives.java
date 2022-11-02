package dev.xdark.ssvm.natives;

import dev.xdark.ssvm.LinkResolver;
import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.api.MethodInvoker;
import dev.xdark.ssvm.api.VMInterface;
import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.Locals;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.execution.VMException;
import dev.xdark.ssvm.inject.InjectedClassLayout;
import dev.xdark.ssvm.mirror.member.JavaField;
import dev.xdark.ssvm.mirror.member.JavaMethod;
import dev.xdark.ssvm.mirror.type.InstanceClass;
import dev.xdark.ssvm.mirror.type.JavaClass;
import dev.xdark.ssvm.operation.VMOperations;
import dev.xdark.ssvm.symbol.Symbols;
import dev.xdark.ssvm.value.InstanceValue;
import dev.xdark.ssvm.value.ObjectValue;
import lombok.experimental.UtilityClass;

import static dev.xdark.ssvm.operation.InvokeDynamicOperations.*;
import static org.objectweb.asm.Opcodes.*;

/**
 * Initializes JSR 292 related components.
 *
 * @author xDark
 */
@UtilityClass
public class MethodHandleNatives {

	private final String VM_INDEX = InjectedClassLayout.java_lang_invoke_MemberName_vmindex.name();

	/**
	 * @param vm VM instance.
	 */
	public void init(VirtualMachine vm) {
		VMInterface vmi = vm.getInterface();
		Symbols symbols = vm.getSymbols();
		InstanceClass natives = symbols.java_lang_invoke_MethodHandleNatives();
		vmi.setInvoker(natives, "registerNatives", "()V", MethodInvoker.noop());
		byte[] speculativeResolve = new byte[]{-1};
		MethodInvoker resolve = ctx -> {
			VMOperations ops = vm.getOperations();
			Locals locals = ctx.getLocals();
			InstanceValue memberName = ops.checkNotNull(locals.loadReference(0));
			resolveMemberName(vm, speculativeResolve[0], locals, memberName);
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
			ctx.setResult(0);
			return Result.ABORT;
		});
		vmi.setInvoker(natives, "init", "(Ljava/lang/invoke/MemberName;Ljava/lang/Object;)V", ctx -> {
			Locals locals = ctx.getLocals();
			VMOperations ops = vm.getOperations();
			InstanceValue memberName = ops.checkNotNull(locals.loadReference(0));
			InstanceValue obj = ops.checkNotNull(locals.loadReference(1));
			InstanceClass objClass = obj.getJavaClass();
			if (objClass == symbols.java_lang_reflect_Method()) {
				initMemberNameMethod(vm, memberName, obj);
			} else if (objClass == symbols.java_lang_reflect_Field()) {
				initMemberNameField(vm, memberName, obj);
			} else if (objClass == symbols.java_lang_reflect_Constructor()) {
				initMemberNameConstructor(vm, memberName, obj);
			} else {
				ops.throwException(symbols.java_lang_InternalError(), "Unsupported class: " + objClass.getName());
			}
			return Result.ABORT;
		});
		vmi.setInvoker(natives, "objectFieldOffset", "(Ljava/lang/invoke/MemberName;)J", ctx -> {
			InstanceValue _this = vm.getOperations().checkNotNull(ctx.getLocals().loadReference(0));
			ctx.setResult((long) vm.getOperations().getInt(_this, VM_INDEX));
			return Result.ABORT;
		});
		vmi.setInvoker(natives, "staticFieldBase", "(Ljava/lang/invoke/MemberName;)Ljava/lang/Object;", ctx -> {
			InstanceValue _this = vm.getOperations().checkNotNull(ctx.getLocals().loadReference(0));
			ctx.setResult(vm.getOperations().getReference(_this, "clazz", "Ljava/lang/Class;"));
			return Result.ABORT;
		});
		vmi.setInvoker(natives, "staticFieldOffset", "(Ljava/lang/invoke/MemberName;)J", ctx -> {
			InstanceValue _this = vm.getOperations().checkNotNull(ctx.getLocals().loadReference(0));
			ctx.setResult((long) vm.getOperations().getInt(_this, VM_INDEX));
			return Result.ABORT;
		});
		vmi.setInvoker(natives, "getMembers", "(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/String;ILjava/lang/Class;I[Ljava/lang/invoke/MemberName;)I", ctx -> {
			ctx.setResult(0);
			return Result.ABORT;
		});

		MethodInvoker setCallSiteTarget = ctx -> {
			Locals locals = ctx.getLocals();
			InstanceValue _this = vm.getOperations().checkNotNull(locals.loadReference(0));
			vm.getOperations().putReference(_this, "target", "Ljava/lang/invoke/MethodHandle;", locals.loadReference(1));
			return Result.ABORT;
		};
		vmi.setInvoker(natives, "setCallSiteTargetNormal", "(Ljava/lang/invoke/CallSite;Ljava/lang/invoke/MethodHandle;)V", setCallSiteTarget);
		vmi.setInvoker(natives, "setCallSiteTargetVolatile", "(Ljava/lang/invoke/CallSite;Ljava/lang/invoke/MethodHandle;)V", setCallSiteTarget);

		InstanceClass mh = symbols.java_lang_invoke_MethodHandle();
		MethodInvoker invoke = ctx -> {
			Locals locals = ctx.getLocals();
			VMOperations ops = vm.getOperations();
			InstanceValue _this = locals.loadReference(0);
			InstanceValue form = ops.checkNotNull(ops.getReference(_this, "form", "Ljava/lang/invoke/LambdaForm;"));
			InstanceValue vmentry = ops.checkNotNull(ops.getReference(form, "vmentry", "Ljava/lang/invoke/MemberName;"));
			JavaMethod vmtarget = ops.readVMTargetFromMemberName(vmentry);
			String name = vmtarget.getName();
			if ("<init>".equals(name)) {
				ops.throwException(symbols.java_lang_InternalError(), "Bad name " + name);
				return Result.ABORT;
			}
			ExecutionContext<?> last = vm.currentOSThread().getBacktrace().peek();
			JavaMethod callerMethod = last.getMethod();
			String callName = callerMethod.getName();
			if ("invoke".equals(callName)) {
				// Ask VM about caller
				JavaClass jc = vm.getReflection().getCallerFrame(2).getMethod().getOwner();
				// Construct MT
				InstanceValue mt = ops.methodType(jc, callerMethod.getType());
				// Invoke asType
				JavaMethod asType = vm.getRuntimeResolver().resolveVirtualMethod(_this, "asType", "(Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/MethodHandle;");
				Locals table = vm.getThreadStorage().newLocals(asType);
				table.setReference(0, _this);
				table.setReference(1, mt);
				_this = (InstanceValue) ops.invokeReference(asType, table);
				// Re-read method target
				form = ops.checkNotNull(ops.getReference(_this, "form", "Ljava/lang/invoke/LambdaForm;"));
				vmentry = ops.checkNotNull(ops.getReference(form, "vmentry", "Ljava/lang/invoke/MemberName;"));
				vmtarget = ops.readVMTargetFromMemberName(vmentry);
				name = vmtarget.getName();
			}

			if ((vmtarget.getModifiers() & ACC_STATIC) == 0) {
				int flags = ops.getInt(vmentry, "flags");
				int refKind = (flags >> MN_REFERENCE_KIND_SHIFT) & MN_REFERENCE_KIND_MASK;
				if (refKind != REF_invokeSpecial && refKind != REF_newInvokeSpecial) {
					vmtarget = vm.getRuntimeResolver().resolveVirtualMethod(_this, name, vmtarget.getDesc());
				}
			}
			Locals table = vm.getThreadStorage().newLocals(vmtarget);
			table.copyFrom(locals, 0, 0, locals.maxSlots());
			table.setReference(0, _this);
			ops.invoke(vmtarget, table, ctx.returnSink());
			return Result.ABORT;
		};
		vmi.setInvoker(mh, "invoke", "([Ljava/lang/Object;)Ljava/lang/Object;", invoke);
		vmi.setInvoker(mh, "invokeBasic", "([Ljava/lang/Object;)Ljava/lang/Object;", invoke);
		vmi.setInvoker(mh, "invokeExact", "([Ljava/lang/Object;)Ljava/lang/Object;", invoke);

		MethodInvoker linkToXX = ctx -> {
			Locals locals = ctx.getLocals();
			VMOperations ops = vm.getOperations();
			InstanceValue memberName = locals.loadReference(locals.maxSlots() - 1);
			InstanceValue resolved = (InstanceValue) ops.getReference(memberName, "method", symbols.java_lang_invoke_ResolvedMethodName().getDescriptor());
			InstanceClass clazz = (InstanceClass) vm.getClassStorage().lookup(ops.checkNotNull(ops.getReference(memberName, "clazz", "Ljava/lang/Class;")));
			JavaMethod vmtarget = clazz.getMethodBySlot(ops.getInt(ops.getReference(resolved, InjectedClassLayout.java_lang_invoke_ResolvedMethodName_vmtarget.name(), InjectedClassLayout.java_lang_invoke_ResolvedMethodName_vmtarget.descriptor()), "value"));

			if ((vmtarget.getModifiers() & ACC_STATIC) == 0) {
				int flags = ops.getInt(memberName, "flags");
				int refKind = (flags >> MN_REFERENCE_KIND_SHIFT) & MN_REFERENCE_KIND_MASK;
				if (refKind != REF_invokeSpecial) {
					ObjectValue instance = locals.loadReference(0);
					ops.checkNotNull(instance);
					vmtarget = vm.getRuntimeResolver().resolveVirtualMethod(instance, vmtarget.getName(), vmtarget.getDesc());
				}
			}
			Locals newLocals = vm.getThreadStorage().newLocals(vmtarget.getMaxLocals());
			newLocals.copyFrom(locals, 0, 0, locals.maxSlots() - 1);
			ops.invoke(vmtarget, newLocals, ctx.returnSink());
			return Result.ABORT;
		};

		vmi.setInvoker(mh, "linkToStatic", "([Ljava/lang/Object;)Ljava/lang/Object;", linkToXX);
		vmi.setInvoker(mh, "linkToVirtual", "([Ljava/lang/Object;)Ljava/lang/Object;", linkToXX);
		vmi.setInvoker(mh, "linkToInterface", "([Ljava/lang/Object;)Ljava/lang/Object;", linkToXX);
		vmi.setInvoker(mh, "linkToSpecial", "([Ljava/lang/Object;)Ljava/lang/Object;", linkToXX);

		InstanceClass lookup = symbols.java_lang_invoke_MethodHandles$Lookup();
		vmi.setInvoker(lookup, "checkAccess", "(BLjava/lang/Class;Ljava/lang/invoke/MemberName;)V", MethodInvoker.noop());

		// TODO impl getMemberVMInfo
		InstanceClass memberName = symbols.java_lang_invoke_MemberName();
		vmi.setInvoker(memberName, "vminfoIsConsistent", "()Z", ctx -> {
			ctx.setResult(1);
			return Result.ABORT;
		});
	}

	private void resolveMemberName(VirtualMachine vm, byte speculativeResolveModeIndex, Locals locals, InstanceValue memberName) {
		VMOperations ops = vm.getOperations();
		InstanceClass clazz = (InstanceClass) vm.getClassStorage().lookup(ops.checkNotNull(ops.getReference(memberName, "clazz", "Ljava/lang/Class;")));
		ops.initialize(clazz);
		String name = ops.readUtf8(ops.getReference(memberName, "name", "Ljava/lang/String;"));
		ObjectValue mt = ops.getReference(memberName, "type", "Ljava/lang/Object;");
		int flags = ops.getInt(memberName, "flags");
		int refKind = (flags >> MN_REFERENCE_KIND_SHIFT) & MN_REFERENCE_KIND_MASK;
		boolean speculativeResolve0 = speculativeResolveModeIndex >= 0 && locals.loadInt(speculativeResolveModeIndex) != 0;
		switch (flags & ALL_KINDS) {
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
				ops.throwException(vm.getSymbols().java_lang_InternalError(), "Not implemented for " + refKind + " " + (flags & ALL_KINDS));
		}
	}

	private void initMemberNameMethod(VirtualMachine vm, InstanceValue memberName, InstanceValue obj) {
		VMOperations ops = vm.getOperations();
		// Copy over clazz, name, type & invoke expand
		InstanceClass clazz = (InstanceClass) vm.getClassStorage().lookup(ops.checkNotNull(ops.getReference(obj, "clazz", "Ljava/lang/Class;")));
		int slot = ops.getInt(obj, "slot");
		JavaMethod method = clazz.getMethodBySlot(slot);

		ops.putReference(memberName, "clazz", "Ljava/lang/Class;", clazz.getOop());
		ops.putReference(memberName, "name", "Ljava/lang/String;", vm.getStringPool().intern(method.getName()));
		InstanceValue mt = ops.methodType(clazz, method.getType());
		ops.putReference(memberName, "type", "Ljava/lang/Object;", mt);
		int refKind;
		if ((method.getModifiers() & ACC_STATIC) == 0) {
			refKind = REF_invokeVirtual;
		} else {
			refKind = REF_invokeStatic;
		}
		ops.initMethodMember(refKind, memberName, method, IS_METHOD);
	}

	private void initMemberNameConstructor(VirtualMachine vm, InstanceValue memberName, InstanceValue obj) {
		VMOperations ops = vm.getOperations();
		// Copy over clazz, name, type & invoke expand
		InstanceClass clazz = (InstanceClass) vm.getClassStorage().lookup(ops.checkNotNull(ops.getReference(obj, "clazz", "Ljava/lang/Class;")));
		int slot = ops.getInt(obj, "slot");
		JavaMethod method = clazz.getMethodBySlot(slot);

		ops.putReference(memberName, "clazz", "Ljava/lang/Class;", clazz.getOop());
		ops.putReference(memberName, "name", "Ljava/lang/String;", vm.getStringPool().intern(method.getName()));
		InstanceValue mt = ops.methodType(clazz, method.getType());
		ops.putReference(memberName, "type", "Ljava/lang/Object;", mt);
		ops.initMethodMember(REF_newInvokeSpecial, memberName, method, IS_CONSTRUCTOR);
	}

	private void initMemberNameField(VirtualMachine vm, InstanceValue memberName, InstanceValue obj) {
		VMOperations ops = vm.getOperations();
		// Copy over clazz, name, type & invoke expand
		InstanceClass clazz = (InstanceClass) vm.getClassStorage().lookup(ops.checkNotNull(ops.getReference(obj, "clazz", "Ljava/lang/Class;")));
		int slot = ops.getInt(obj, "slot");
		JavaField field = clazz.getFieldBySlot(slot);
		ops.putReference(memberName, "clazz", "Ljava/lang/Class;", clazz.getOop());
		ops.putReference(memberName, "name", "Ljava/lang/String;", vm.getStringPool().intern(field.getName()));
		InstanceValue mt = field.getType().getOop();
		ops.putReference(memberName, "type", "Ljava/lang/Object;", mt);
		int refKind;
		if ((field.getModifiers() & ACC_STATIC) == 0) {
			refKind = REF_getField;
		} else {
			refKind = REF_getStatic;
		} // JDK code will change that later on.
		ops.initFieldMember(refKind, memberName, field);
	}

	private void initMethodMember(int refKind, VirtualMachine vm, InstanceValue memberName, InstanceClass clazz, String name, ObjectValue methodType, int type, boolean speculativeResolve0) {
		VMOperations ops = vm.getOperations();
		Symbols symbols = vm.getSymbols();
		LinkResolver linkResolver = vm.getLinkResolver();
		JavaMethod method = vm.getRuntimeResolver().resolveVirtualMethod(methodType, "toMethodDescriptorString", "()Ljava/lang/String;");
		Locals locals = vm.getThreadStorage().newLocals(method);
		locals.setReference(0, methodType);
		String desc = ops.readUtf8(ops.invokeReference(method, locals));
		JavaMethod handle;
		lookup:
		{
			try {
				switch (refKind) {
					case REF_invokeStatic:
						handle = clazz.getMethod(name, desc);
						break lookup;
					case REF_invokeSpecial:
					case REF_newInvokeSpecial:
					case REF_invokeVirtual:
						handle = linkResolver.resolveVirtualMethod(clazz, name, desc);
						break lookup;
					case REF_invokeInterface:
						handle = linkResolver.resolveInterfaceMethod(clazz, name, desc);
						break lookup;
				}
				ops.throwException(symbols.java_lang_InternalError(), "unrecognized MemberName format");
				return;
			} catch (VMException ignored) {
				// VM threw NoSuchMethodError
				handle = null;
			}
		}
		if (handle == null) {
			if (!speculativeResolve0) {
				return;
			}
			ops.throwException(symbols.java_lang_NoSuchMethodError(), clazz.getInternalName() + '.' + name + desc);
		}
		ops.initMethodMember(refKind, memberName, handle, type);
	}

	private void initFieldMember(int refKind, VirtualMachine vm, InstanceValue memberName, InstanceClass clazz, String name, ObjectValue type, boolean speculativeResolve0) {
		VMOperations ops = vm.getOperations();
		Symbols symbols = vm.getSymbols();
		String desc = vm.getClassStorage().lookup(ops.checkNotNull(type)).getDescriptor();

		// TODO hotspot "feature"?
		// https://github.com/openjdk/jdk/blob/026b85303c01326bc49a1105a89853d7641fcd50/src/hotspot/share/prims/methodHandles.cpp#L839
		// https://github.com/openjdk/jdk/blob/534e557874274255c55086b4f6128063cbd9cc58/src/hotspot/share/interpreter/linkResolver.cpp#L974
		JavaField handle;
		switch (refKind) {
			case REF_getStatic:
			case REF_putStatic:
			case REF_getField:
			case REF_putField:
				handle = clazz.getField(name, desc);
				break;
			default:
				ops.throwException(symbols.java_lang_InternalError(), "unrecognized MemberName format");
				return;
		}
		if (handle == null) {
			if (speculativeResolve0) {
				return;
			}
			ops.throwException(symbols.java_lang_NoSuchFieldError(), name);
		}
		ops.initFieldMember(refKind, memberName, handle);
	}
}
