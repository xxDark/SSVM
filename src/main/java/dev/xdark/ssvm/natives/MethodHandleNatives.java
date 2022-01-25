package dev.xdark.ssvm.natives;

import dev.xdark.ssvm.NativeJava;
import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.api.MethodInvoker;
import dev.xdark.ssvm.execution.PanicException;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.mirror.JavaClass;
import dev.xdark.ssvm.mirror.JavaField;
import dev.xdark.ssvm.mirror.JavaMethod;
import dev.xdark.ssvm.value.*;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.objectweb.asm.Type;

import java.util.Arrays;

import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Opcodes.ACC_STATIC;

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
	private final int RECOGNIZED_METHOD_MODIFIERS = ACC_PUBLIC |
			ACC_PRIVATE |
			ACC_PROTECTED |
			ACC_STATIC |
			ACC_FINAL |
			ACC_SYNCHRONIZED |
			ACC_BRIDGE |
			ACC_VARARGS |
			ACC_NATIVE |
			ACC_ABSTRACT |
			ACC_STRICT |
			ACC_SYNTHETIC;
	private final int RECOGNIZED_FIELD_MODIFIERS = ACC_PUBLIC |
			ACC_PRIVATE |
			ACC_PROTECTED |
			ACC_STATIC |
			ACC_FINAL |
			ACC_VOLATILE |
			ACC_TRANSIENT |
			ACC_ENUM |
			ACC_SYNTHETIC;

	/**
	 * @param vm
	 * 		VM instance.
	 */
	public void init(VirtualMachine vm) {
		val vmi = vm.getInterface();
		val symbols = vm.getSymbols();
		val natives = symbols.java_lang_invoke_MethodHandleNatives;
		vmi.setInvoker(natives, "registerNatives", "()V", MethodInvoker.noop());
		val resolve = (MethodInvoker) ctx -> {
			val helper = vm.getHelper();
			val memberName = helper.<InstanceValue>checkNotNull(ctx.getLocals().load(0));
			val classWrapper = (JavaValue<InstanceJavaClass>) memberName.getValue("clazz", "Ljava/lang/Class;");
			val clazz = classWrapper.getValue();
			val name = helper.readUtf8(memberName.getValue("name", "Ljava/lang/String;"));
			val mt = memberName.getValue("type", "Ljava/lang/Object;");
			int flags = memberName.getInt("flags");
			int refKind = (flags >> MN_REFERENCE_KIND_SHIFT) & MN_REFERENCE_KIND_MASK;
			switch (flags & ALL_KINDS) {
				case IS_METHOD:
					initMethodMember(refKind, vm, memberName, clazz, name, mt, IS_METHOD);
					break;
				case IS_FIELD:
					initFieldMember(refKind, vm, memberName, clazz, name, mt);
					break;
				case IS_CONSTRUCTOR:
					initMethodMember(refKind, vm, memberName, clazz, name, mt, IS_CONSTRUCTOR);
					break;
				default:
					helper.throwException(symbols.java_lang_InternalError, "Not implemented for " + refKind + " " + (flags & ALL_KINDS));
			}

			ctx.setResult(memberName);
			return Result.ABORT;
		};
		if (!vmi.setInvoker(natives, "resolve", "(Ljava/lang/invoke/MemberName;Ljava/lang/Class;Z)Ljava/lang/invoke/MemberName;", resolve)) {
			if (!vmi.setInvoker(natives, "resolve", "(Ljava/lang/invoke/MemberName;Ljava/lang/Class;)Ljava/lang/invoke/MemberName;", resolve)) {
				throw new IllegalStateException("Unable to locate MethodHandleNatives#resolve method");
			}
		}
		vmi.setInvoker(natives, "getConstant", "(I)I", ctx -> {
			ctx.setResult(IntValue.ZERO);
			return Result.ABORT;
		});
		vmi.setInvoker(natives, "init", "(Ljava/lang/invoke/MemberName;Ljava/lang/Object;)V", ctx -> {
			val locals = ctx.getLocals();
			val helper = ctx.getHelper();
			val memberName = helper.<InstanceValue>checkNotNull(locals.load(0));
			val obj = helper.<InstanceValue>checkNotNull(locals.load(1));
			val objClass = obj.getJavaClass();
			if (objClass == symbols.java_lang_reflect_Method) {
				initMemberNameMethod(vm, memberName, obj);
			} else if (objClass == symbols.java_lang_reflect_Field) {
				initMemberNameField(vm, memberName, obj);
			} else {
				helper.throwException(symbols.java_lang_InternalError, "Not implemented");
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

		val setCallSiteTarget = (MethodInvoker) ctx -> {
			val locals = ctx.getLocals();
			locals.<InstanceValue>load(0).setValue("target", "Ljava/lang/invoke/MethodHandle;", locals.load(1));
			return Result.ABORT;
		};
		vmi.setInvoker(natives, "setCallSiteTargetNormal", "(Ljava/lang/invoke/CallSite;Ljava/lang/invoke/MethodHandle;)V", setCallSiteTarget);
		vmi.setInvoker(natives, "setCallSiteTargetVolatile", "(Ljava/lang/invoke/CallSite;Ljava/lang/invoke/MethodHandle;)V", setCallSiteTarget);

		val mh = symbols.java_lang_invoke_MethodHandle;
		val invoke = (MethodInvoker) ctx -> {
			val locals = ctx.getLocals();
			val helper = vm.getHelper();
			val _this = locals.<InstanceValue>load(0);
			val form = helper.<InstanceValue>checkNotNull(_this.getValue("form", "Ljava/lang/invoke/LambdaForm;"));
			val vmentry = helper.<InstanceValue>checkNotNull(form.getValue("vmentry", "Ljava/lang/invoke/MemberName;"));
			val resolved = (InstanceValue) vmentry.getValue("method", symbols.java_lang_invoke_ResolvedMethodName.getDescriptor());
			val vmtarget = ((JavaValue<Object>) resolved.getValue(VM_TARGET, "Ljava/lang/Object;")).getValue();
			if (vmtarget instanceof JavaMethod) {
				val jm = (JavaMethod) vmtarget;
				val name = jm.getName();
				if ("<init>".equals(name)) {
					throw new PanicException("TODO");
				}
				Value[] lvt = locals.getTable();
				if (jm.getMaxArgs() > lvt.length) {
					// TODO figure uot what the ... is going on?
					val last = lvt[lvt.length - 1];
					if (last instanceof ArrayValue) {
						val arr = (ArrayValue) last;
						int length = arr.getLength();
						int x = lvt.length;
						lvt = Arrays.copyOf(lvt, x + length - 1);
						for (int i = 0, k = x + length; x < k; x++) {
							lvt[x - 1] = arr.getValue(i++);
						}
					}
				}

				Value result;
				if ((jm.getAccess() & ACC_STATIC) == 0) {
					result = helper.invokeVirtual(name, jm.getDesc(), new Value[0], lvt).getResult();
				} else {
					result = helper.invokeStatic(jm.getOwner(), jm, new Value[0], lvt).getResult();
				}
				ctx.setResult(voidAsNull(result, ctx.getMethod()));
			} else {
				throw new PanicException("TODO: " + vmtarget);
			}
			return Result.ABORT;
		};
		vmi.setInvoker(mh, "invoke", "([Ljava/lang/Object;)Ljava/lang/Object;", invoke);
		vmi.setInvoker(mh, "invokeBasic", "([Ljava/lang/Object;)Ljava/lang/Object;", invoke);
		vmi.setInvoker(mh, "invokeExact", "([Ljava/lang/Object;)Ljava/lang/Object;", invoke);

		val linkToXX = (MethodInvoker) ctx -> {
			val locals = ctx.getLocals();
			val helper = vm.getHelper();
			val table = locals.getTable();
			int length = table.length;
			val memberName = locals.<InstanceValue>load(length - 1);
			val resolved = (InstanceValue) memberName.getValue("method", symbols.java_lang_invoke_ResolvedMethodName.getDescriptor());
			val vmtarget = ((JavaValue<JavaMethod>) resolved.getValue(VM_TARGET, "Ljava/lang/Object;")).getValue();

			val args = Arrays.copyOfRange(locals.getTable(), 0, length - 1);

			Value result;
			if ((vmtarget.getAccess() & ACC_STATIC) == 0) {
				result = helper.invokeVirtual(vmtarget.getName(), vmtarget.getDesc(), new Value[0], args).getResult();
			} else {
				result = helper.invokeStatic(vmtarget.getOwner(), vmtarget, new Value[0], args).getResult();
			}
			ctx.setResult(voidAsNull(result, ctx.getMethod()));
			return Result.ABORT;
		};

		vmi.setInvoker(mh, "linkToStatic", "([Ljava/lang/Object;)Ljava/lang/Object;", linkToXX);
		vmi.setInvoker(mh, "linkToVirtual", "([Ljava/lang/Object;)Ljava/lang/Object;", linkToXX);
		vmi.setInvoker(mh, "linkToInterface", "([Ljava/lang/Object;)Ljava/lang/Object;", linkToXX);

		val lookup = symbols.java_lang_invoke_MethodHandles$Lookup;
		vmi.setInvoker(lookup, "checkAccess", "(BLjava/lang/Class;Ljava/lang/invoke/MemberName;)V", MethodInvoker.noop());
	}

	private void initMemberNameMethod(VirtualMachine vm, InstanceValue memberName, InstanceValue obj) {
		val helper = vm.getHelper();
		// Copy over clazz, name, type & invoke expand
		val clazz = ((JavaValue<InstanceJavaClass>) obj.getValue("clazz", "Ljava/lang/Class;")).getValue();
		val slot = obj.getInt("slot");
		val method = helper.getMethodBySlot(clazz, slot);

		memberName.setValue("clazz", "Ljava/lang/Class;", clazz.getOop());
		memberName.setValue("name", "Ljava/lang/String;", vm.getStringPool().intern(method.getName()));
		val mt = helper.methodType(clazz.getClassLoader(), method.getType());
		memberName.setValue("type", "Ljava/lang/Object;", mt);
		int refKind;
		if ((method.getAccess() & ACC_STATIC) == 0) {
			refKind = REF_invokeVirtual;
		} else {
			refKind = REF_invokeStatic;
		}
		initMethodMember(refKind, vm, memberName, method, IS_METHOD);
	}

	private void initMemberNameField(VirtualMachine vm, InstanceValue memberName, InstanceValue obj) {
		val helper = vm.getHelper();
		// Copy over clazz, name, type & invoke expand
		val clazz = ((JavaValue<InstanceJavaClass>) obj.getValue("clazz", "Ljava/lang/Class;")).getValue();
		val slot = obj.getInt("slot");
		val field = helper.getFieldBySlot(clazz, slot);
		memberName.setValue("clazz", "Ljava/lang/Class;", clazz.getOop());
		memberName.setValue("name", "Ljava/lang/String;", vm.getStringPool().intern(field.getName()));
		val mt = helper.findClass(clazz.getClassLoader(), field.getType().getInternalName(), false).getOop();
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
		val symbols = vm.getSymbols();
		// Inject vmholder & vmtarget into resolved name
		memberName.setInt(VM_INDEX, handle.getSlot());
		val memoryManager = vm.getMemoryManager();
		val rmn = symbols.java_lang_invoke_ResolvedMethodName;
		rmn.initialize();
		val resolvedName = memoryManager.newInstance(rmn);
		resolvedName.initialize();
		val jlo = symbols.java_lang_Object;
		resolvedName.setValue(VM_TARGET, "Ljava/lang/Object;", memoryManager.newJavaInstance(jlo, handle));
		resolvedName.setValue(VM_HOLDER, "Ljava/lang/Object;", handle.getOwner().getOop());
		memberName.setValue("method", symbols.java_lang_invoke_ResolvedMethodName.getDescriptor(), resolvedName);
		// Inject flags
		int flags = handle.getAccess() & RECOGNIZED_METHOD_MODIFIERS;
		flags |= mnType | (refKind << MN_REFERENCE_KIND_SHIFT);
		memberName.setInt("flags", flags);
	}

	private void initFieldMember(int refKind, VirtualMachine vm, InstanceValue memberName, JavaField handle) {
		val symbols = vm.getSymbols();
		// Inject vmholder & vmtarget into resolved name
		val memoryManager = vm.getMemoryManager();
		val owner = handle.getOwner();
		long offset = handle.getOffset();
		if ((handle.getAccess() & ACC_STATIC) == 0) {
			offset += memoryManager.valueBaseOffset(owner);
		} else {
			offset += memoryManager.getStaticOffset(owner);
		}
		memberName.setInt(VM_INDEX, (int) offset);
		val rmn = symbols.java_lang_invoke_ResolvedMethodName;
		rmn.initialize();
		val resolvedName = memoryManager.newInstance(rmn);
		resolvedName.initialize();
		val jlo = symbols.java_lang_Object;
		resolvedName.setValue(VM_TARGET, "Ljava/lang/Object;", memoryManager.newJavaInstance(jlo, handle));
		resolvedName.setValue(VM_HOLDER, "Ljava/lang/Object;", owner.getOop());
		memberName.setValue("method", symbols.java_lang_invoke_ResolvedMethodName.getDescriptor(), resolvedName);
		// Inject flags
		int flags = handle.getAccess() & RECOGNIZED_FIELD_MODIFIERS;
		flags |= IS_FIELD | (refKind << MN_REFERENCE_KIND_SHIFT);
		memberName.setInt("flags", flags);
	}

	private void initMethodMember(int refKind, VirtualMachine vm, InstanceValue memberName, InstanceJavaClass clazz, String name, Value methodType, int type) {
		val helper = vm.getHelper();
		val symbols = vm.getSymbols();
		val desc = helper.readUtf8(helper.invokeExact(symbols.java_lang_invoke_MethodType, "toMethodDescriptorString", "()Ljava/lang/String;", new Value[0], new Value[]{
				methodType
		}).getResult());
		JavaMethod handle;
		switch (refKind) {
			case REF_invokeStatic:
				handle = clazz.getStaticMethod(name, desc);
				break;
			case REF_invokeVirtual:
			case REF_invokeSpecial:
			case REF_newInvokeSpecial:
			case REF_invokeInterface:
				handle = clazz.getVirtualMethod(name, desc);
				break;
			default:
				throw new PanicException("TODO ? " + refKind);
		}
		if (handle == null) {
			helper.throwException(symbols.java_lang_NoSuchMethodError, clazz.getInternalName() + '.' + name + desc);
		}
		initMethodMember(refKind, vm, memberName, handle, type);
	}

	private void initFieldMember(int refKind, VirtualMachine vm, InstanceValue memberName, InstanceJavaClass clazz, String name, Value type) {
		val helper = vm.getHelper();
		val symbols = vm.getSymbols();
		val desc = ((JavaValue<JavaClass>) type).getValue().getDescriptor();
		JavaField handle;
		switch (refKind) {
			case REF_getStatic:
			case REF_putStatic:
				handle = clazz.getStaticField(name, desc);
				break;
			case REF_getField:
			case REF_putField:
				handle = clazz.getVirtualField(name, desc);
				break;
			default:
				throw new PanicException("TODO ?");
		}
		if (handle == null) {
			helper.throwException(symbols.java_lang_NoSuchFieldError, name);
		}
		initFieldMember(refKind, vm, memberName, handle);
	}

	private Value voidAsNull(Value v, JavaMethod jm) {
		if (v.isVoid()) {
			// Return null if return type is non-void
			if (jm.getReturnType() != Type.VOID_TYPE) {
				return NullValue.INSTANCE;
			}
		}
		return v;
	}
}
