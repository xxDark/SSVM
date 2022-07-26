package dev.xdark.ssvm.natives;

import dev.xdark.ssvm.NativeJava;
import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.api.MethodInvoker;
import dev.xdark.ssvm.api.VMInterface;
import dev.xdark.ssvm.asm.Modifier;
import dev.xdark.ssvm.execution.Locals;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.memory.management.MemoryManager;
import dev.xdark.ssvm.memory.management.StringPool;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.mirror.JavaClass;
import dev.xdark.ssvm.mirror.JavaField;
import dev.xdark.ssvm.mirror.JavaMethod;
import dev.xdark.ssvm.symbol.VMPrimitives;
import dev.xdark.ssvm.util.VMHelper;
import dev.xdark.ssvm.symbol.VMSymbols;
import dev.xdark.ssvm.value.ArrayValue;
import dev.xdark.ssvm.value.InstanceValue;
import dev.xdark.ssvm.value.IntValue;
import dev.xdark.ssvm.value.JavaValue;
import dev.xdark.ssvm.value.ObjectValue;
import dev.xdark.ssvm.value.Value;
import lombok.experimental.UtilityClass;
import me.coley.cafedude.classfile.ClassFile;
import me.coley.cafedude.classfile.AttributeConstants;
import me.coley.cafedude.classfile.ClassMember;
import me.coley.cafedude.classfile.ConstPool;
import me.coley.cafedude.classfile.Method;
import me.coley.cafedude.classfile.attribute.AnnotationDefaultAttribute;
import me.coley.cafedude.classfile.attribute.AnnotationsAttribute;
import me.coley.cafedude.classfile.attribute.Attribute;
import me.coley.cafedude.classfile.attribute.ParameterAnnotationsAttribute;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InnerClassNode;

import java.util.List;

/**
 * Initializes java/lang/Class.
 *
 * @author xDark
 * @noinspection DuplicatedCode
 */
@UtilityClass
public class ClassNatives {

	private final String CONSTANT_POOL = NativeJava.CONSTANT_POOL;
	private final String PROTECTION_DOMAIN = NativeJava.PROTECTION_DOMAIN;

	/**
	 * @param vm VM instance.
	 */
	public void init(VirtualMachine vm) {
		VMInterface vmi = vm.getInterface();
		VMSymbols symbols = vm.getSymbols();
		InstanceJavaClass jlc = symbols.java_lang_Class();
		vmi.setInvoker(jlc, "registerNatives", "()V", MethodInvoker.noop());
		vmi.setInvoker(jlc, "getPrimitiveClass", "(Ljava/lang/String;)Ljava/lang/Class;", ctx -> {
			String name = vm.getHelper().readUtf8(ctx.getLocals().load(0));
			VMPrimitives primitives = vm.getPrimitives();
			Value result;
			switch (name) {
				case "long":
					result = primitives.longPrimitive().getOop();
					break;
				case "double":
					result = primitives.doublePrimitive().getOop();
					break;
				case "int":
					result = primitives.intPrimitive().getOop();
					break;
				case "float":
					result = primitives.floatPrimitive().getOop();
					break;
				case "char":
					result = primitives.charPrimitive().getOop();
					break;
				case "short":
					result = primitives.shortPrimitive().getOop();
					break;
				case "byte":
					result = primitives.bytePrimitive().getOop();
					break;
				case "boolean":
					result = primitives.booleanPrimitive().getOop();
					break;
				case "void":
					result = primitives.voidPrimitive().getOop();
					break;
				default:
					vm.getHelper().throwException(symbols.java_lang_IllegalArgumentException());
					result = null;
			}
			ctx.setResult(result);
			return Result.ABORT;
		});
		vmi.setInvoker(jlc, "desiredAssertionStatus0", "(Ljava/lang/Class;)Z", ctx -> {
			ctx.setResult(IntValue.ZERO);
			return Result.ABORT;
		});
		vmi.setInvoker(jlc, "forName0", "(Ljava/lang/String;ZLjava/lang/ClassLoader;Ljava/lang/Class;)Ljava/lang/Class;", ctx -> {
			Locals locals = ctx.getLocals();
			VMHelper helper = vm.getHelper();
			String name = helper.readUtf8(helper.checkNotNull(locals.load(0)));
			boolean initialize = locals.load(1).asBoolean();
			ObjectValue loader = locals.load(2);
			JavaClass klass = helper.findClass(loader, name.replace('.', '/'), initialize);
			if (Modifier.isHiddenMember(klass.getModifiers())) {
				helper.throwException(symbols.java_lang_ClassNotFoundException(), name);
			}
			ctx.setResult(klass.getOop());
			return Result.ABORT;
		});
		MethodInvoker classNameInit = ctx -> {
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
			Locals locals = ctx.getLocals();
			VMHelper helper = vm.getHelper();
			JavaClass _this = locals.<JavaValue<JavaClass>>load(0).getValue();
			JavaClass arg = helper.<JavaValue<JavaClass>>checkNotNull(locals.load(1)).getValue();
			ctx.setResult(_this.isAssignableFrom(arg) ? IntValue.ONE : IntValue.ZERO);
			return Result.ABORT;
		});
		vmi.setInvoker(jlc, "isInterface", "()Z", ctx -> {
			Locals locals = ctx.getLocals();
			JavaClass _this = locals.<JavaValue<JavaClass>>load(0).getValue();
			ctx.setResult(_this.isInterface() ? IntValue.ONE : IntValue.ZERO);
			return Result.ABORT;
		});
		vmi.setInvoker(jlc, "isPrimitive", "()Z", ctx -> {
			Locals locals = ctx.getLocals();
			JavaClass _this = locals.<JavaValue<JavaClass>>load(0).getValue();
			ctx.setResult(_this.isPrimitive() ? IntValue.ONE : IntValue.ZERO);
			return Result.ABORT;
		});
		vmi.setInvoker(jlc, "isHidden", "()Z", ctx -> {
			Locals locals = ctx.getLocals();
			JavaClass _this = locals.<JavaValue<JavaClass>>load(0).getValue();
			ctx.setResult(Modifier.isHiddenMember(_this.getModifiers()) ? IntValue.ONE : IntValue.ZERO);
			return Result.ABORT;
		});
		vmi.setInvoker(jlc, "getSuperclass", "()Ljava/lang/Class;", ctx -> {
			Locals locals = ctx.getLocals();
			JavaClass _this = locals.<JavaValue<JavaClass>>load(0).getValue();
			InstanceJavaClass superClass = _this.getSuperClass();
			ctx.setResult(superClass == null ? vm.getMemoryManager().nullValue() : superClass.getOop());
			return Result.ABORT;
		});
		vmi.setInvoker(jlc, "getModifiers", "()I", ctx -> {
			Locals locals = ctx.getLocals();
			JavaClass _this = locals.<JavaValue<JavaClass>>load(0).getValue();
			ctx.setResult(IntValue.of(Modifier.eraseClass(_this.getModifiers())));
			return Result.ABORT;
		});
		vmi.setInvoker(jlc, "getDeclaredConstructors0", "(Z)[Ljava/lang/reflect/Constructor;", ctx -> {
			Locals locals = ctx.getLocals();
			JavaClass klass = locals.<JavaValue<JavaClass>>load(0).getValue();
			VMHelper helper = vm.getHelper();
			InstanceJavaClass constructorClass = symbols.java_lang_reflect_Constructor();
			if (!(klass instanceof InstanceJavaClass)) {
				ArrayValue empty = helper.emptyArray(constructorClass);
				ctx.setResult(empty);
				return Result.ABORT;
			}
			klass.initialize();
			StringPool pool = vm.getStringPool();
			boolean publicOnly = locals.load(1).asBoolean();
			List<JavaMethod> methods = ((InstanceJavaClass) klass).getDeclaredConstructors(publicOnly);
			ObjectValue loader = klass.getClassLoader();
			ArrayValue result = helper.newArray(constructorClass, methods.size());
			InstanceValue callerOop = klass.getOop();
			MemoryManager memoryManager = vm.getMemoryManager();
			for (int j = 0; j < methods.size(); j++) {
				JavaMethod mn = methods.get(j);
				Type[] types = mn.getArgumentTypes();
				ArrayValue parameters = helper.convertClasses(helper.convertTypes(loader, types, false));
				ArrayValue exceptions = convertExceptions(helper, loader, mn.getNode().exceptions);
				MethodRawData data = getMethodRawData(mn, false);
				InstanceValue constructor = memoryManager.newInstance(constructorClass);
				constructor.setValue("clazz", "Ljava/lang/Class;", callerOop);
				constructor.setInt("slot", mn.getSlot());
				constructor.setValue("parameterTypes", "[Ljava/lang/Class;", parameters);
				constructor.setValue("exceptionTypes", "[Ljava/lang/Class;", exceptions);
				constructor.setInt("modifiers", Modifier.eraseMethod(mn.getAccess()));
				constructor.setValue("signature", "Ljava/lang/String;", pool.intern(mn.getSignature()));
				constructor.setValue("annotations", "[B", data.annotations);
				constructor.setValue("parameterAnnotations", "[B", data.parameterAnnotations);
				constructor.initialize();
				result.setValue(j, constructor);
			}
			ctx.setResult(result);
			return Result.ABORT;
		});
		vmi.setInvoker(jlc, "getDeclaredMethods0", "(Z)[Ljava/lang/reflect/Method;", ctx -> {
			Locals locals = ctx.getLocals();
			JavaClass klass = locals.<JavaValue<JavaClass>>load(0).getValue();
			VMHelper helper = vm.getHelper();
			InstanceJavaClass methodClass = symbols.java_lang_reflect_Method();
			if (!(klass instanceof InstanceJavaClass)) {
				ArrayValue empty = helper.emptyArray(methodClass);
				ctx.setResult(empty);
				return Result.ABORT;
			}
			klass.initialize();
			StringPool pool = vm.getStringPool();
			boolean publicOnly = locals.load(1).asBoolean();
			List<JavaMethod> methods = ((InstanceJavaClass) klass).getDeclaredMethods(publicOnly);
			ObjectValue loader = klass.getClassLoader();
			ArrayValue result = helper.newArray(methodClass, methods.size());
			InstanceValue callerOop = klass.getOop();
			MemoryManager memoryManager = vm.getMemoryManager();
			for (int j = 0; j < methods.size(); j++) {
				JavaMethod mn = methods.get(j);
				Type[] types = mn.getArgumentTypes();
				JavaClass rt = helper.findClass(loader, mn.getReturnType().getInternalName(), false);
				ArrayValue parameters = helper.convertClasses(helper.convertTypes(loader, types, false));
				ArrayValue exceptions = convertExceptions(helper, loader, mn.getNode().exceptions);
				MethodRawData data = getMethodRawData(mn, true);
				InstanceValue method = memoryManager.newInstance(methodClass);
				method.setValue("clazz", "Ljava/lang/Class;", callerOop);
				method.setInt("slot", mn.getSlot());
				method.setValue("name", "Ljava/lang/String;", pool.intern(mn.getName()));
				method.setValue("returnType", "Ljava/lang/Class;", rt.getOop());
				method.setValue("parameterTypes", "[Ljava/lang/Class;", parameters);
				method.setValue("exceptionTypes", "[Ljava/lang/Class;", exceptions);
				method.setInt("modifiers", Modifier.eraseMethod(mn.getAccess()));
				method.setValue("signature", "Ljava/lang/String;", pool.intern(mn.getSignature()));
				method.setValue("annotations", "[B", data.annotations);
				method.setValue("parameterAnnotations", "[B", data.parameterAnnotations);
				method.setValue("annotationDefault", "[B", data.annotationDefault);
				method.initialize();
				result.setValue(j, method);
			}
			ctx.setResult(result);
			return Result.ABORT;
		});
		vmi.setInvoker(jlc, "getDeclaredFields0", "(Z)[Ljava/lang/reflect/Field;", ctx -> {
			Locals locals = ctx.getLocals();
			JavaClass klass = locals.<JavaValue<JavaClass>>load(0).getValue();
			VMHelper helper = vm.getHelper();
			InstanceJavaClass fieldClass = symbols.java_lang_reflect_Field();
			fieldClass.initialize();
			if (!(klass instanceof InstanceJavaClass)) {
				ArrayValue empty = helper.emptyArray(fieldClass);
				ctx.setResult(empty);
				return Result.ABORT;
			}
			klass.initialize();
			StringPool pool = vm.getStringPool();
			boolean publicOnly = locals.load(1).asBoolean();
			List<JavaField> fields = ((InstanceJavaClass) klass).getDeclaredFields(publicOnly);
			ObjectValue loader = klass.getClassLoader();
			ArrayValue result = helper.newArray(fieldClass, fields.size());
			InstanceValue callerOop = klass.getOop();
			MemoryManager memoryManager = vm.getMemoryManager();
			for (int j = 0; j < fields.size(); j++) {
				JavaField fn = fields.get(j);
				JavaClass type = helper.findClass(loader, fn.getType().getInternalName(), false);
				InstanceValue field = memoryManager.newInstance(fieldClass);
				field.setValue("clazz", "Ljava/lang/Class;", callerOop);
				field.setInt("slot", fn.getSlot());
				field.setValue("name", "Ljava/lang/String;", pool.intern(fn.getName()));
				field.setValue("type", "Ljava/lang/Class;", type.getOop());
				field.setInt("modifiers", Modifier.eraseField(fn.getAccess()));
				field.setValue("signature", "Ljava/lang/String;", pool.intern(fn.getSignature()));
				field.setValue("annotations", "[B", readFieldAnnotations(fn));
				field.initialize();
				result.setValue(j, field);
			}
			ctx.setResult(result);
			return Result.ABORT;
		});
		vmi.setInvoker(jlc, "getInterfaces0", "()[Ljava/lang/Class;", ctx -> {
			JavaClass _this = ctx.getLocals().<JavaValue<JavaClass>>load(0).getValue();
			InstanceJavaClass[] interfaces = _this.getInterfaces();
			ArrayValue types = vm.getHelper().convertClasses(interfaces);
			ctx.setResult(types);
			return Result.ABORT;
		});
		vmi.setInvoker(jlc, "getEnclosingMethod0", "()[Ljava/lang/Object;", ctx -> {
			JavaClass klasas = ((JavaValue<JavaClass>) ctx.getLocals().load(0)).getValue();
			if (!(klasas instanceof InstanceJavaClass)) {
				ctx.setResult(vm.getMemoryManager().nullValue());
			} else {
				ClassNode node = ((InstanceJavaClass) klasas).getNode();
				String enclosingClass = node.outerClass;
				String enclosingMethod = node.outerMethod;
				String enclosingDesc = node.outerMethodDesc;
				if (enclosingClass == null || enclosingMethod == null || enclosingDesc == null) {
					ctx.setResult(vm.getMemoryManager().nullValue());
				} else {
					VMHelper helper = vm.getHelper();
					StringPool pool = vm.getStringPool();
					JavaClass outerHost = helper.findClass(ctx.getOwner().getClassLoader(), enclosingClass, false);
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
			JavaClass klasas = ((JavaValue<JavaClass>) ctx.getLocals().load(0)).getValue();
			if (!(klasas instanceof InstanceJavaClass)) {
				ctx.setResult(vm.getMemoryManager().nullValue());
			} else {
				ClassNode node = ((InstanceJavaClass) klasas).getNode();
				String nestHostClass = node.nestHostClass;
				if (nestHostClass == null) {
					ctx.setResult(vm.getMemoryManager().nullValue());
				} else {
					VMHelper helper = vm.getHelper();
					JavaClass oop = helper.findClass(ctx.getOwner().getClassLoader(), nestHostClass, false);
					ctx.setResult(oop.getOop());
				}
			}
			return Result.ABORT;
		});
		vmi.setInvoker(jlc, "getSimpleBinaryName0", "()Ljava/lang/String;", ctx -> {
			JavaClass klasas = ((JavaValue<JavaClass>) ctx.getLocals().load(0)).getValue();
			if (!(klasas instanceof InstanceJavaClass)) {
				ctx.setResult(vm.getMemoryManager().nullValue());
			} else {
				String name = klasas.getInternalName();
				int idx = name.lastIndexOf('$');
				if (idx != -1) {
					ctx.setResult(vm.getStringPool().intern(name.substring(idx + 1)));
				} else {
					ctx.setResult(vm.getMemoryManager().nullValue());
				}
			}
			return Result.ABORT;
		});
		vmi.setInvoker(jlc, "isInstance", "(Ljava/lang/Object;)Z", ctx -> {
			Locals locals = ctx.getLocals();
			Value value = locals.load(1);
			if (value.isNull()) {
				ctx.setResult(IntValue.ZERO);
			} else {
				JavaClass klass = ((ObjectValue) value).getJavaClass();
				JavaClass _this = locals.<JavaValue<JavaClass>>load(0).getValue();
				ctx.setResult(_this.isAssignableFrom(klass) ? IntValue.ONE : IntValue.ZERO);
			}
			return Result.ABORT;
		});
		vmi.setInvoker(jlc, "getComponentType", "()Ljava/lang/Class;", ctx -> {
			JavaClass type = ctx.getLocals().<JavaValue<JavaClass>>load(0).getValue().getComponentType();
			ctx.setResult(type == null ? vm.getMemoryManager().nullValue() : type.getOop());
			return Result.ABORT;
		});
		vmi.setInvoker(jlc, "getProtectionDomain0", "()Ljava/security/ProtectionDomain;", ctx -> {
			InstanceValue _this = ctx.getLocals().load(0);
			ctx.setResult(_this.getValue(PROTECTION_DOMAIN, "Ljava/security/ProtectionDomain;"));
			return Result.ABORT;
		});
		vmi.setInvoker(jlc, "getRawAnnotations", "()[B", ctx -> {
			JavaClass _this = ctx.getLocals().<JavaValue<JavaClass>>load(0).getValue();
			if (!(_this instanceof InstanceJavaClass)) {
				ctx.setResult(vm.getMemoryManager().nullValue());
				return Result.ABORT;
			}
			ClassFile classFile = ((InstanceJavaClass) _this).getRawClassFile();
			ConstPool cp = classFile.getPool();
			ctx.setResult(getAnnotationsIn(vm, cp, classFile.getAttributes()));
			return Result.ABORT;
		});
		vmi.setInvoker(jlc, "getDeclaredClasses0", "()[Ljava/lang/Class;", ctx -> {
			JavaClass _this = ctx.getLocals().<JavaValue<JavaClass>>load(0).getValue();
			VMHelper helper = vm.getHelper();
			if (!(_this instanceof InstanceJavaClass)) {
				ctx.setResult(helper.emptyArray(jlc));
			} else {
				List<InnerClassNode> declaredClasses = ((InstanceJavaClass) _this).getNode().innerClasses;
				ObjectValue loader = _this.getClassLoader();
				ArrayValue array = helper.newArray(jlc, declaredClasses.size());
				for (int i = 0; i < declaredClasses.size(); i++) {
					array.setValue(i, helper.findClass(loader, declaredClasses.get(i).name, false).getOop());
				}
				ctx.setResult(array);
			}
			return Result.ABORT;
		});

		InstanceJavaClass cpClass = symbols.reflect_ConstantPool();
		vmi.setInvoker(jlc, "getConstantPool", "()" + cpClass.getDescriptor(), ctx -> {
			InstanceValue _this = ctx.getLocals().load(0);
			ObjectValue cp = _this.getValue(CONSTANT_POOL, "Ljava/lang/Object;");
			if (cp.isNull()) {
				cpClass.initialize();
				InstanceValue instance = vm.getMemoryManager().newInstance(cpClass);
				instance.setValue("constantPoolOop", "Ljava/lang/Object;", _this);
				instance.initialize();
				_this.setValue(CONSTANT_POOL, "Ljava/lang/Object;", instance);
				cp = instance;
			}
			ctx.setResult(cp);
			return Result.ABORT;
		});
	}

	private ObjectValue readFieldAnnotations(JavaField field) {
		InstanceJavaClass owner = field.getOwner();
		ClassFile cf = owner.getRawClassFile();
		return getAnnotationsOf(owner.getVM(), cf.getFields(), cf.getPool(), field.getName(), field.getDesc());
	}

	private ObjectValue getAnnotationsOf(VirtualMachine vm, List<? extends ClassMember> members, ConstPool cp, String name, String desc) {
		for (ClassMember candidate : members) {
			String cname = cp.getUtf(candidate.getNameIndex());
			if (!name.equals(cname)) {
				continue;
			}
			String cdesc = cp.getUtf(candidate.getTypeIndex());
			if (desc.equals(cdesc)) {
				return getAnnotationsIn(vm, cp, candidate.getAttributes());
			}
		}
		return vm.getMemoryManager().nullValue();
	}

	private ObjectValue getAnnotationsIn(VirtualMachine vm, ConstPool cp, List<Attribute> attributes) {
		return attributes.stream()
			.filter(x -> AttributeConstants.RUNTIME_VISIBLE_ANNOTATIONS.equals(cp.getUtf(x.getNameIndex())))
			.findFirst()
			.map(x -> readAnnotation(x, vm))
			.orElse(vm.getMemoryManager().nullValue());
	}

	private ObjectValue readAnnotation(Attribute attr, VirtualMachine vm) {
		VMHelper helper = vm.getHelper();
		if (!(attr instanceof AnnotationsAttribute)) {
			helper.throwException(vm.getSymbols().java_lang_IllegalStateException(), "Invalid annotation");
		}
		return helper.toVMBytes(Util.toBytes((AnnotationsAttribute) attr));
	}

	private ObjectValue readParameterAnnotations(Attribute attr, VirtualMachine vm) {
		VMHelper helper = vm.getHelper();
		if (!(attr instanceof ParameterAnnotationsAttribute)) {
			helper.throwException(vm.getSymbols().java_lang_IllegalStateException(), "Invalid annotation");
		}
		return helper.toVMBytes(Util.toBytes((ParameterAnnotationsAttribute) attr));
	}

	private ObjectValue readAnnotationDefault(Attribute attr, VirtualMachine vm) {
		VMHelper helper = vm.getHelper();
		if (!(attr instanceof AnnotationDefaultAttribute)) {
			helper.throwException(vm.getSymbols().java_lang_IllegalStateException(), "Invalid annotation");
		}
		return helper.toVMBytes(Util.toBytes((AnnotationDefaultAttribute) attr));
	}

	private MethodRawData getMethodRawData(JavaMethod jm, boolean includeDefault) {
		String name = jm.getName();
		String desc = jm.getDesc();
		InstanceJavaClass owner = jm.getOwner();
		VirtualMachine vm = owner.getVM();
		MethodRawData data = new MethodRawData(vm.getMemoryManager().nullValue());
		ClassFile cf = owner.getRawClassFile();
		List<Method> methods = cf.getMethods();
		ConstPool cp = cf.getPool();
		search:
		for (Method candidate : methods) {
			if (!name.equals(cp.getUtf(candidate.getNameIndex()))) {
				continue;
			}
			if (!desc.equals(cp.getUtf(candidate.getTypeIndex()))) {
				continue;
			}
			for (Attribute attr : candidate.getAttributes()) {
				String attrName = cp.getUtf(attr.getNameIndex());
				if (AttributeConstants.RUNTIME_VISIBLE_ANNOTATIONS.equals(attrName)) {
					data.annotations = readAnnotation(attr, vm);
				} else if (AttributeConstants.RUNTIME_VISIBLE_PARAMETER_ANNOTATIONS.equals(attrName)) {
					data.parameterAnnotations = readParameterAnnotations(attr, vm);
				} else if (includeDefault && AttributeConstants.ANNOTATION_DEFAULT.equals(attrName)) {
					data.annotationDefault = readAnnotationDefault(attr, vm);
				}
				if (data.isComplete(includeDefault)) {
					break search;
				}
			}
		}
		return data;
	}

	private ArrayValue convertExceptions(VMHelper helper, ObjectValue loader, List<String> exceptions) {
		InstanceJavaClass jlc = helper.getVM().getSymbols().java_lang_Class();
		if (exceptions == null || exceptions.isEmpty()) {
			return helper.emptyArray(jlc);
		}
		ArrayValue array = helper.newArray(jlc, exceptions.size());
		for (int i = 0; i < exceptions.size(); i++) {
			array.setValue(i, helper.findClass(loader, exceptions.get(i), false).getOop());
		}
		return array;
	}

	private static final class MethodRawData {

		ObjectValue annotations;
		ObjectValue parameterAnnotations;
		ObjectValue annotationDefault;

		MethodRawData(ObjectValue nullValue) {
			annotations = nullValue;
			parameterAnnotations = nullValue;
			annotationDefault = nullValue;
		}

		boolean isComplete(boolean includeDefault) {
			return !annotations.isNull() && !parameterAnnotations.isNull()
				&& (!includeDefault || !annotationDefault.isNull());
		}
	}
}
