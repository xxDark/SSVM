package dev.xdark.ssvm.natives;

import dev.xdark.ssvm.NativeJava;
import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.api.MethodInvoker;
import dev.xdark.ssvm.asm.Modifier;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.mirror.JavaClass;
import dev.xdark.ssvm.mirror.JavaField;
import dev.xdark.ssvm.mirror.JavaMethod;
import dev.xdark.ssvm.value.*;
import lombok.experimental.UtilityClass;
import lombok.val;
import me.coley.cafedude.ClassMember;
import me.coley.cafedude.ConstPool;
import me.coley.cafedude.Constants;
import me.coley.cafedude.attribute.AnnotationsAttribute;
import me.coley.cafedude.attribute.Attribute;

import java.util.List;

/**
 * Initializes java/lang/Class.
 *
 * @author xDark
 */
@UtilityClass
public class ClassNatives {

	private final String CONSTANT_POOL = NativeJava.CONSTANT_POOL;
	private final String PROTECTION_DOMAIN = NativeJava.PROTECTION_DOMAIN;

	/**
	 * @param vm
	 * 		VM instance.
	 */
	public void init(VirtualMachine vm) {
		val vmi = vm.getInterface();
		val symbols = vm.getSymbols();
		val jlc = symbols.java_lang_Class;
		vmi.setInvoker(jlc, "registerNatives", "()V", MethodInvoker.noop());
		vmi.setInvoker(jlc, "getPrimitiveClass", "(Ljava/lang/String;)Ljava/lang/Class;", ctx -> {
			val name = vm.getHelper().readUtf8(ctx.getLocals().load(0));
			val primitives = vm.getPrimitives();
			Value result;
			switch (name) {
				case "long":
					result = primitives.longPrimitive.getOop();
					break;
				case "double":
					result = primitives.doublePrimitive.getOop();
					break;
				case "int":
					result = primitives.intPrimitive.getOop();
					break;
				case "float":
					result = primitives.floatPrimitive.getOop();
					break;
				case "char":
					result = primitives.charPrimitive.getOop();
					break;
				case "short":
					result = primitives.shortPrimitive.getOop();
					break;
				case "byte":
					result = primitives.bytePrimitive.getOop();
					break;
				case "boolean":
					result = primitives.booleanPrimitive.getOop();
					break;
				case "void":
					result = primitives.voidPrimitive.getOop();
					break;
				default:
					vm.getHelper().throwException(symbols.java_lang_IllegalArgumentException);
					result = null;
			}
			ctx.setResult(result);
			return Result.ABORT;
		});
		vmi.setInvoker(jlc, "desiredAssertionStatus0", "(Ljava/lang/Class;)Z", ctx -> {
			ctx.setResult(IntValue.ONE);
			return Result.ABORT;
		});
		vmi.setInvoker(jlc, "forName0", "(Ljava/lang/String;ZLjava/lang/ClassLoader;Ljava/lang/Class;)Ljava/lang/Class;", ctx -> {
			val locals = ctx.getLocals();
			val helper = vm.getHelper();
			val name = helper.readUtf8(helper.checkNotNull(locals.load(0)));
			val initialize = locals.load(1).asBoolean();
			val loader = locals.load(2);
			val klass = helper.findClass(loader, name.replace('.', '/'), initialize);
			if (Modifier.isHiddenMember(klass.getModifiers())) {
				helper.throwException(symbols.java_lang_ClassNotFoundException, name);
			}
			ctx.setResult(klass.getOop());
			return Result.ABORT;
		});
		val classNameInit = (MethodInvoker) ctx -> {
			ctx.setResult(vm.getStringPool().intern(ctx.getLocals().<JavaValue<JavaClass>>load(0).getValue().getName()));
			return Result.ABORT;
		};
		if (!vmi.setInvoker(jlc, "getName0", "()Ljava/lang/String;", classNameInit)) {
			if (!vmi.setInvoker(jlc, "initClassName", "()Ljava/lang/String;", classNameInit)) {
				throw new IllegalStateException("Unable to locate Class name init method");
			}
		}
		vmi.setInvoker(jlc, "isArray", "()Z", ctx -> {
			ctx.setResult(ctx.getLocals().<JavaValue<JavaClass>>load(0).getValue().isArray() ? IntValue.ONE : IntValue.ZERO);
			return Result.ABORT;
		});
		vmi.setInvoker(jlc, "isAssignableFrom", "(Ljava/lang/Class;)Z", ctx -> {
			val locals = ctx.getLocals();
			val helper = vm.getHelper();
			val _this = locals.<JavaValue<JavaClass>>load(0).getValue();
			val arg = helper.<JavaValue<JavaClass>>checkNotNull(locals.load(1)).getValue();
			ctx.setResult(_this.isAssignableFrom(arg) ? IntValue.ONE : IntValue.ZERO);
			return Result.ABORT;
		});
		vmi.setInvoker(jlc, "isInterface", "()Z", ctx -> {
			val locals = ctx.getLocals();
			val _this = locals.<JavaValue<JavaClass>>load(0).getValue();
			ctx.setResult(_this.isInterface() ? IntValue.ONE : IntValue.ZERO);
			return Result.ABORT;
		});
		vmi.setInvoker(jlc, "isPrimitive", "()Z", ctx -> {
			val locals = ctx.getLocals();
			val _this = locals.<JavaValue<JavaClass>>load(0).getValue();
			ctx.setResult(_this.isPrimitive() ? IntValue.ONE : IntValue.ZERO);
			return Result.ABORT;
		});
		vmi.setInvoker(jlc, "getSuperclass", "()Ljava/lang/Class;", ctx -> {
			val locals = ctx.getLocals();
			val _this = locals.<JavaValue<JavaClass>>load(0).getValue();
			val superClass = _this.getSuperClass();
			ctx.setResult(superClass == null ? NullValue.INSTANCE : superClass.getOop());
			return Result.ABORT;
		});
		vmi.setInvoker(jlc, "getModifiers", "()I", ctx -> {
			val locals = ctx.getLocals();
			val _this = locals.<JavaValue<JavaClass>>load(0).getValue();
			ctx.setResult(IntValue.of(Modifier.erase(_this.getModifiers())));
			return Result.ABORT;
		});
		vmi.setInvoker(jlc, "getDeclaredConstructors0", "(Z)[Ljava/lang/reflect/Constructor;", ctx -> {
			val locals = ctx.getLocals();
			val klass = locals.<JavaValue<JavaClass>>load(0).getValue();
			val helper = vm.getHelper();
			if (!(klass instanceof InstanceJavaClass)) {
				val empty = helper.emptyArray(symbols.java_lang_reflect_Constructor);
				ctx.setResult(empty);
			} else {
				klass.initialize();
				val pool = vm.getStringPool();
				val publicOnly = locals.load(1).asBoolean();
				val methods = ((InstanceJavaClass) klass).getDeclaredConstructors(publicOnly);
				val loader = klass.getClassLoader();
				val refFactory = symbols.reflect_ReflectionFactory;
				val reflectionFactory = (InstanceValue) helper.invokeStatic(refFactory, "getReflectionFactory", "()" + refFactory.getDescriptor(), new Value[0], new Value[0]).getResult();
				val result = helper.newArray(symbols.java_lang_reflect_Constructor, methods.size());
				val classArray = helper.emptyArray(symbols.java_lang_Class);
				val callerOop = klass.getOop();
				for (int j = 0; j < methods.size(); j++) {
					val mn = methods.get(j);
					val types = mn.getArgumentTypes();
					val parameters = helper.convertClasses(helper.convertTypes(loader, types, false));
					val c = helper.invokeVirtual("newConstructor", "(Ljava/lang/Class;[Ljava/lang/Class;[Ljava/lang/Class;IILjava/lang/String;[B[B)Ljava/lang/reflect/Constructor;", new Value[0], new Value[]{
							reflectionFactory,
							callerOop,
							parameters,
							classArray,
							IntValue.of(Modifier.erase(mn.getAccess())),
							IntValue.of(mn.getSlot()),
							pool.intern(mn.getSignature()),
							readMethodAnnotations(mn),
							NullValue.INSTANCE
					}).getResult();
					result.setValue(j, (ObjectValue) c);
				}
				ctx.setResult(result);
			}
			return Result.ABORT;
		});
		vmi.setInvoker(jlc, "getDeclaredMethods0", "(Z)[Ljava/lang/reflect/Method;", ctx -> {
			val locals = ctx.getLocals();
			JavaClass klass = locals.<JavaValue<JavaClass>>load(0).getValue();
			val helper = vm.getHelper();
			if (!(klass instanceof InstanceJavaClass)) {
				val empty = helper.emptyArray(symbols.java_lang_reflect_Method);
				ctx.setResult(empty);
				return Result.ABORT;
			}
			klass.initialize();
			val pool = vm.getStringPool();
			val publicOnly = locals.load(1).asBoolean();
			val methods = ((InstanceJavaClass) klass).getDeclaredMethods(publicOnly);
			val loader = klass.getClassLoader();
			val refFactory = symbols.reflect_ReflectionFactory;
			val reflectionFactory = (InstanceValue) helper.invokeStatic(refFactory, "getReflectionFactory", "()" + refFactory.getDescriptor(), new Value[0], new Value[0]).getResult();
			val result = helper.newArray(symbols.java_lang_reflect_Method, methods.size());
			val classArray = helper.emptyArray(symbols.java_lang_Class);
			val callerOop = klass.getOop();
			for (int j = 0; j < methods.size(); j++) {
				val mn = methods.get(j);
				val types = mn.getArgumentTypes();
				val rt = helper.findClass(loader, mn.getReturnType().getInternalName(), false);
				val parameters = helper.convertClasses(helper.convertTypes(loader, types, false));
				val c = helper.invokeVirtual("newMethod", "(Ljava/lang/Class;Ljava/lang/String;[Ljava/lang/Class;Ljava/lang/Class;[Ljava/lang/Class;IILjava/lang/String;[B[B[B)Ljava/lang/reflect/Method;", new Value[0], new Value[]{
						reflectionFactory,
						callerOop,
						pool.intern(mn.getName()),
						parameters,
						rt.getOop(),
						classArray,
						IntValue.of(Modifier.erase(mn.getAccess())),
						IntValue.of(mn.getSlot()),
						pool.intern(mn.getSignature()),
						readMethodAnnotations(mn),
						NullValue.INSTANCE,
						NullValue.INSTANCE
				}).getResult();
				result.setValue(j, (ObjectValue) c);
			}
			ctx.setResult(result);
			return Result.ABORT;
		});
		vmi.setInvoker(jlc, "getDeclaredFields0", "(Z)[Ljava/lang/reflect/Field;", ctx -> {
			val locals = ctx.getLocals();
			val klass = locals.<JavaValue<JavaClass>>load(0).getValue();
			val helper = vm.getHelper();
			if (!(klass instanceof InstanceJavaClass)) {
				val empty = helper.emptyArray(symbols.java_lang_reflect_Field);
				ctx.setResult(empty);
			} else {
				klass.initialize();
				val pool = vm.getStringPool();
				val publicOnly = locals.load(1).asBoolean();
				val fields = ((InstanceJavaClass) klass).getDeclaredFields(publicOnly);
				val loader = klass.getClassLoader();
				val refFactory = symbols.reflect_ReflectionFactory;
				val reflectionFactory = (InstanceValue) helper.invokeStatic(refFactory, "getReflectionFactory", "()" + refFactory.getDescriptor(), new Value[0], new Value[0]).getResult();
				val result = helper.newArray(symbols.java_lang_reflect_Field, fields.size());
				val callerOop = klass.getOop();
				for (int j = 0; j < fields.size(); j++) {
					val fn = fields.get(j);
					val type = helper.findClass(loader, fn.getType().getInternalName(), false);
					val c = helper.invokeVirtual("newField", "(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/Class;IILjava/lang/String;[B)Ljava/lang/reflect/Field;", new Value[0], new Value[]{
							reflectionFactory,
							callerOop,
							pool.intern(fn.getName()),
							type.getOop(),
							IntValue.of(Modifier.erase(fn.getAccess())),
							IntValue.of(fn.getSlot()),
							pool.intern(fn.getSignature()),
							readFieldAnnotations(fn)
					}).getResult();
					result.setValue(j, (ObjectValue) c);
				}
				ctx.setResult(result);
			}
			return Result.ABORT;
		});
		vmi.setInvoker(jlc, "getInterfaces0", "()[Ljava/lang/Class;", ctx -> {
			val _this = ctx.getLocals().<JavaValue<JavaClass>>load(0).getValue();
			val interfaces = _this.getInterfaces();
			val types = vm.getHelper().convertClasses(interfaces);
			ctx.setResult(types);
			return Result.ABORT;
		});
		vmi.setInvoker(jlc, "getEnclosingMethod0", "()[Ljava/lang/Object;", ctx -> {
			val klasas = ((JavaValue<JavaClass>) ctx.getLocals().load(0)).getValue();
			if (!(klasas instanceof InstanceJavaClass)) {
				ctx.setResult(NullValue.INSTANCE);
			} else {
				val node = ((InstanceJavaClass) klasas).getNode();
				val enclosingClass = node.outerClass;
				val enclosingMethod = node.outerMethod;
				val enclosingDesc = node.outerMethodDesc;
				if (enclosingClass == null || enclosingMethod == null || enclosingDesc == null) {
					ctx.setResult(NullValue.INSTANCE);
				} else {
					val helper = vm.getHelper();
					val pool = vm.getStringPool();
					val outerHost = helper.findClass(ctx.getOwner().getClassLoader(), enclosingClass, false);
					ctx.setResult(helper.toVMValues(new ObjectValue[]{
							outerHost.getOop(),
							pool.intern(enclosingMethod),
							pool.intern(enclosingDesc)
					}));
				}
			}
			return Result.ABORT;
		});
		vmi.setInvoker(jlc, "getDeclaringClass0", "()Ljava/lang/Class;", ctx -> {
			val klasas = ((JavaValue<JavaClass>) ctx.getLocals().load(0)).getValue();
			if (!(klasas instanceof InstanceJavaClass)) {
				ctx.setResult(NullValue.INSTANCE);
			} else {
				val node = ((InstanceJavaClass) klasas).getNode();
				val nestHostClass = node.nestHostClass;
				if (nestHostClass == null) {
					ctx.setResult(NullValue.INSTANCE);
				} else {
					val helper = vm.getHelper();
					val oop = helper.findClass(ctx.getOwner().getClassLoader(), nestHostClass, false);
					ctx.setResult(oop.getOop());
				}
			}
			return Result.ABORT;
		});
		vmi.setInvoker(jlc, "getSimpleBinaryName0", "()Ljava/lang/String;", ctx -> {
			val klasas = ((JavaValue<JavaClass>) ctx.getLocals().load(0)).getValue();
			if (!(klasas instanceof InstanceJavaClass)) {
				ctx.setResult(NullValue.INSTANCE);
			} else {
				val name = klasas.getInternalName();
				val idx = name.lastIndexOf('$');
				if (idx != -1) {
					ctx.setResult(vm.getStringPool().intern(name.substring(idx + 1)));
				} else {
					ctx.setResult(NullValue.INSTANCE);
				}
			}
			return Result.ABORT;
		});
		vmi.setInvoker(jlc, "isInstance", "(Ljava/lang/Object;)Z", ctx -> {
			val locals = ctx.getLocals();
			val value = locals.load(1);
			if (value.isNull()) {
				ctx.setResult(IntValue.ZERO);
			} else {
				val klass = ((ObjectValue) value).getJavaClass();
				val _this = locals.<JavaValue<JavaClass>>load(0).getValue();
				ctx.setResult(_this.isAssignableFrom(klass) ? IntValue.ONE : IntValue.ZERO);
			}
			return Result.ABORT;
		});
		vmi.setInvoker(jlc, "getComponentType", "()Ljava/lang/Class;", ctx -> {
			val type = ctx.getLocals().<JavaValue<JavaClass>>load(0).getValue().getComponentType();
			ctx.setResult(type == null ? NullValue.INSTANCE : type.getOop());
			return Result.ABORT;
		});
		vmi.setInvoker(jlc, "getProtectionDomain0", "()Ljava/security/ProtectionDomain;", ctx -> {
			val _this = ctx.getLocals().<InstanceValue>load(0);
			ctx.setResult(_this.getValue(PROTECTION_DOMAIN, "Ljava/security/ProtectionDomain;"));
			return Result.ABORT;
		});
		vmi.setInvoker(jlc, "getRawAnnotations", "()[B", ctx -> {
			val _this = ctx.getLocals().<JavaValue<JavaClass>>load(0).getValue();
			if (!(_this instanceof InstanceJavaClass)) {
				ctx.setResult(NullValue.INSTANCE);
				return Result.ABORT;
			}
			val classFile = ((InstanceJavaClass) _this).getRawClassFile();
			val cp = classFile.getPool();
			ctx.setResult(getAnnotationsIn(vm, cp, classFile.getAttributes()));
			return Result.ABORT;
		});
		vmi.setInvoker(jlc, "getDeclaredClasses0", "()[Ljava/lang/Class;", ctx -> {
			val _this = ctx.getLocals().<JavaValue<JavaClass>>load(0).getValue();
			val helper = vm.getHelper();
			if (!(_this instanceof InstanceJavaClass)) {
				ctx.setResult(helper.emptyArray(jlc));
			} else {
				val declaredClasses = ((InstanceJavaClass) _this).getNode().innerClasses;
				val loader = _this.getClassLoader();
				val array = helper.newArray(jlc, declaredClasses.size());
				for (int i = 0; i < declaredClasses.size(); i++) {
					array.setValue(i, helper.findClass(loader, declaredClasses.get(i).name, false).getOop());
				}
				ctx.setResult(array);
			}
			return Result.ABORT;
		});

		val cpClass = symbols.reflect_ConstantPool;
		vmi.setInvoker(jlc, "getConstantPool", "()" + cpClass.getDescriptor(), ctx -> {
			val _this = ctx.getLocals().<InstanceValue>load(0);
			ObjectValue cp = _this.getValue(CONSTANT_POOL, "Ljava/lang/Object;");
			if (cp.isNull()) {
				cpClass.initialize();
				val instance = vm.getMemoryManager().newInstance(cpClass);
				instance.initialize();
				instance.setValue("constantPoolOop", "Ljava/lang/Object;", _this);
				_this.setValue(CONSTANT_POOL, "Ljava/lang/Object;", instance);
				cp = instance;
			}
			ctx.setResult(cp);
			return Result.ABORT;
		});
	}

	private ObjectValue readFieldAnnotations(JavaField field) {
		val owner = field.getOwner();
		val cf = owner.getRawClassFile();
		return getAnnotationsOf(owner.getVM(), cf.getFields(), cf.getPool(), field.getName(), field.getDesc());
	}

	private ObjectValue readMethodAnnotations(JavaMethod method) {
		val owner = method.getOwner();
		val cf = owner.getRawClassFile();
		return getAnnotationsOf(owner.getVM(), cf.getMethods(), cf.getPool(), method.getName(), method.getDesc());
	}

	private ObjectValue getAnnotationsOf(VirtualMachine vm, List<? extends ClassMember> members, ConstPool cp, String name, String desc) {
		for (val candidate : members) {
			val cname = cp.getUtf(candidate.getNameIndex());
			if (!name.equals(cname)) continue;
			val cdesc = cp.getUtf(candidate.getTypeIndex());
			if (desc.equals(cdesc)) {
				return getAnnotationsIn(vm, cp, candidate.getAttributes());
			}
		}
		return NullValue.INSTANCE;
	}

	private ObjectValue getAnnotationsIn(VirtualMachine vm, ConstPool cp, List<Attribute> attributes) {
		val opt = attributes.stream()
				.filter(x -> Constants.Attributes.RUNTIME_VISIBLE_ANNOTATIONS.equals(cp.getUtf(x.getNameIndex())))
				.findFirst();
		if (opt.isPresent()) {
			val attr = opt.get();
			val helper = vm.getHelper();
			if (!(attr instanceof AnnotationsAttribute)) {
				helper.throwException(vm.getSymbols().java_lang_IllegalStateException, "Invalid annotation");
			}
			return helper.toVMBytes(Util.toBytes((AnnotationsAttribute) attr));
		}
		return NullValue.INSTANCE;
	}
}
