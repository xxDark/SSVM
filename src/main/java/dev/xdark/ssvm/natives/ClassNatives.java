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
import dev.xdark.ssvm.mirror.type.InstanceJavaClass;
import dev.xdark.ssvm.mirror.type.JavaClass;
import dev.xdark.ssvm.mirror.member.JavaField;
import dev.xdark.ssvm.mirror.member.JavaMethod;
import dev.xdark.ssvm.symbol.VMPrimitives;
import dev.xdark.ssvm.util.VMHelper;
import dev.xdark.ssvm.symbol.VMSymbols;
import dev.xdark.ssvm.util.VMOperations;
import dev.xdark.ssvm.value.ArrayValue;
import dev.xdark.ssvm.value.InstanceValue;
import dev.xdark.ssvm.value.JavaValue;
import dev.xdark.ssvm.value.ObjectValue;
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
			String name = vm.getHelper().readUtf8(ctx.getLocals().loadReference(0));
			VMPrimitives primitives = vm.getPrimitives();
			ObjectValue result;
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
			ctx.setResult(0);
			return Result.ABORT;
		});
		vmi.setInvoker(jlc, "forName0", "(Ljava/lang/String;ZLjava/lang/ClassLoader;Ljava/lang/Class;)Ljava/lang/Class;", ctx -> {
			Locals locals = ctx.getLocals();
			VMHelper helper = vm.getHelper();
			String name = helper.readUtf8(helper.checkNotNull(locals.loadReference(0)));
			boolean initialize = locals.loadInt(1) != 0;
			ObjectValue loader = locals.loadReference(2);
			JavaClass klass = helper.findClass(loader, name.replace('.', '/'), initialize);
			if (Modifier.isHiddenMember(klass.getModifiers())) {
				helper.throwException(symbols.java_lang_ClassNotFoundException(), name);
			}
			ctx.setResult(klass.getOop());
			return Result.ABORT;
		});
		MethodInvoker classNameInit = ctx -> {
			ctx.setResult(vm.getStringPool().intern(ctx.getLocals().<JavaValue<JavaClass>>loadReference(0).getValue().getName()));
			return Result.ABORT;
		};
		if (!vmi.setInvoker(jlc, "getName0", "()Ljava/lang/String;", classNameInit)) {
			if (!vmi.setInvoker(jlc, "initClassName", "()Ljava/lang/String;", classNameInit)) {
				throw new IllegalStateException("Unable to locate Class name init method");
			}
		}
		vmi.setInvoker(jlc, "isArray", "()Z", ctx -> {
			ctx.setResult(ctx.getLocals().<JavaValue<JavaClass>>loadReference(0).getValue().isArray() ? 1 : 0);
			return Result.ABORT;
		});
		vmi.setInvoker(jlc, "isAssignableFrom", "(Ljava/lang/Class;)Z", ctx -> {
			Locals locals = ctx.getLocals();
			VMHelper helper = vm.getHelper();
			JavaClass _this = locals.<JavaValue<JavaClass>>loadReference(0).getValue();
			JavaClass arg = helper.<JavaValue<JavaClass>>checkNotNull(locals.loadReference(1)).getValue();
			ctx.setResult(_this.isAssignableFrom(arg) ? 1 : 0);
			return Result.ABORT;
		});
		vmi.setInvoker(jlc, "isInterface", "()Z", ctx -> {
			Locals locals = ctx.getLocals();
			JavaClass _this = locals.<JavaValue<JavaClass>>loadReference(0).getValue();
			ctx.setResult(_this.isInterface() ? 1 : 0);
			return Result.ABORT;
		});
		vmi.setInvoker(jlc, "isPrimitive", "()Z", ctx -> {
			Locals locals = ctx.getLocals();
			JavaClass _this = locals.<JavaValue<JavaClass>>loadReference(0).getValue();
			ctx.setResult(_this.isPrimitive() ? 1 : 0);
			return Result.ABORT;
		});
		vmi.setInvoker(jlc, "isHidden", "()Z", ctx -> {
			Locals locals = ctx.getLocals();
			JavaClass _this = locals.<JavaValue<JavaClass>>loadReference(0).getValue();
			ctx.setResult(Modifier.isHiddenMember(_this.getModifiers()) ? 1 : 0);
			return Result.ABORT;
		});
		vmi.setInvoker(jlc, "getSuperclass", "()Ljava/lang/Class;", ctx -> {
			Locals locals = ctx.getLocals();
			JavaClass _this = locals.<JavaValue<JavaClass>>loadReference(0).getValue();
			InstanceJavaClass superClass = _this.getSuperClass();
			ctx.setResult(superClass == null ? vm.getMemoryManager().nullValue() : superClass.getOop());
			return Result.ABORT;
		});
		vmi.setInvoker(jlc, "getModifiers", "()I", ctx -> {
			Locals locals = ctx.getLocals();
			JavaClass _this = locals.<JavaValue<JavaClass>>loadReference(0).getValue();
			ctx.setResult(Modifier.eraseClass(_this.getModifiers()));
			return Result.ABORT;
		});
		vmi.setInvoker(jlc, "getDeclaredConstructors0", "(Z)[Ljava/lang/reflect/Constructor;", ctx -> {
			Locals locals = ctx.getLocals();
			JavaClass klass = locals.<JavaValue<JavaClass>>loadReference(0).getValue();
			VMHelper helper = vm.getHelper();
			InstanceJavaClass constructorClass = symbols.java_lang_reflect_Constructor();
			if (!(klass instanceof InstanceJavaClass)) {
				ArrayValue empty = helper.emptyArray(constructorClass);
				ctx.setResult(empty);
				return Result.ABORT;
			}
			klass.initialize();
			StringPool pool = vm.getStringPool();
			boolean publicOnly = locals.loadInt(1) != 0;
			List<JavaMethod> methods = ((InstanceJavaClass) klass).getDeclaredConstructors(publicOnly);
			ObjectValue loader = klass.getClassLoader();
			ArrayValue result = helper.newArray(constructorClass, methods.size());
			InstanceValue callerOop = klass.getOop();
			MemoryManager memoryManager = vm.getMemoryManager();
			VMOperations ops = vm.getPublicOperations();
			for (int j = 0; j < methods.size(); j++) {
				JavaMethod mn = methods.get(j);
				JavaClass[] types = mn.getArgumentTypes();
				ArrayValue parameters = helper.convertClasses(types);
				ArrayValue exceptions = convertExceptions(helper, loader, mn.getNode().exceptions);
				MethodRawData data = getMethodRawData(mn, false);
				InstanceValue constructor = memoryManager.newInstance(constructorClass);
				ops.putReference(constructor, "clazz", "Ljava/lang/Class;", callerOop);
				ops.putInt(constructor, "slot", mn.getSlot());
				ops.putReference(constructor, "parameterTypes", "[Ljava/lang/Class;", parameters);
				ops.putReference(constructor, "exceptionTypes", "[Ljava/lang/Class;", exceptions);
				ops.putInt(constructor, "modifiers", Modifier.eraseMethod(mn.getModifiers()));
				ops.putReference(constructor, "signature", "Ljava/lang/String;", pool.intern(mn.getSignature()));
				ops.putReference(constructor, "annotations", "[B", data.annotations);
				ops.putReference(constructor, "parameterAnnotations", "[B", data.parameterAnnotations);
				result.setReference(j, constructor);
			}
			ctx.setResult(result);
			return Result.ABORT;
		});
		vmi.setInvoker(jlc, "getDeclaredMethods0", "(Z)[Ljava/lang/reflect/Method;", ctx -> {
			Locals locals = ctx.getLocals();
			JavaClass klass = locals.<JavaValue<JavaClass>>loadReference(0).getValue();
			VMHelper helper = vm.getHelper();
			InstanceJavaClass methodClass = symbols.java_lang_reflect_Method();
			if (!(klass instanceof InstanceJavaClass)) {
				ArrayValue empty = helper.emptyArray(methodClass);
				ctx.setResult(empty);
				return Result.ABORT;
			}
			klass.initialize();
			StringPool pool = vm.getStringPool();
			boolean publicOnly = locals.loadInt(1) != 0;
			List<JavaMethod> methods = ((InstanceJavaClass) klass).getDeclaredMethods(publicOnly);
			ObjectValue loader = klass.getClassLoader();
			ArrayValue result = helper.newArray(methodClass, methods.size());
			InstanceValue callerOop = klass.getOop();
			MemoryManager memoryManager = vm.getMemoryManager();
			VMOperations ops = vm.getPublicOperations();
			for (int j = 0; j < methods.size(); j++) {
				JavaMethod mn = methods.get(j);
				JavaClass[] types = mn.getArgumentTypes();
				JavaClass rt =mn.getReturnType();
				ArrayValue parameters = helper.convertClasses(types);
				ArrayValue exceptions = convertExceptions(helper, loader, mn.getNode().exceptions);
				MethodRawData data = getMethodRawData(mn, true);
				InstanceValue method = memoryManager.newInstance(methodClass);
				ops.putReference(method, "clazz", "Ljava/lang/Class;", callerOop);
				ops.putInt(method, "slot", mn.getSlot());
				ops.putReference(method, "name", "Ljava/lang/String;", pool.intern(mn.getName()));
				ops.putReference(method, "returnType", "Ljava/lang/Class;", rt.getOop());
				ops.putReference(method, "parameterTypes", "[Ljava/lang/Class;", parameters);
				ops.putReference(method, "exceptionTypes", "[Ljava/lang/Class;", exceptions);
				ops.putInt(method, "modifiers", Modifier.eraseMethod(mn.getModifiers()));
				ops.putReference(method, "signature", "Ljava/lang/String;", pool.intern(mn.getSignature()));
				ops.putReference(method, "annotations", "[B", data.annotations);
				ops.putReference(method, "parameterAnnotations", "[B", data.parameterAnnotations);
				ops.putReference(method, "annotationDefault", "[B", data.annotationDefault);
				result.setReference(j, method);
			}
			ctx.setResult(result);
			return Result.ABORT;
		});
		vmi.setInvoker(jlc, "getDeclaredFields0", "(Z)[Ljava/lang/reflect/Field;", ctx -> {
			Locals locals = ctx.getLocals();
			JavaClass klass = locals.<JavaValue<JavaClass>>loadReference(0).getValue();
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
			boolean publicOnly = locals.loadInt(1) != 0;
			List<JavaField> fields = ((InstanceJavaClass) klass).getDeclaredFields(publicOnly);
			ObjectValue loader = klass.getClassLoader();
			ArrayValue result = helper.newArray(fieldClass, fields.size());
			InstanceValue callerOop = klass.getOop();
			MemoryManager memoryManager = vm.getMemoryManager();
			VMOperations ops = vm.getPublicOperations();
			for (int j = 0; j < fields.size(); j++) {
				JavaField fn = fields.get(j);
				JavaClass type = fn.getType();
				InstanceValue field = memoryManager.newInstance(fieldClass);
				ops.putReference(field, "clazz", "Ljava/lang/Class;", callerOop);
				ops.putInt(field, "slot", fn.getSlot());
				ops.putReference(field, "name", "Ljava/lang/String;", pool.intern(fn.getName()));
				ops.putReference(field, "type", "Ljava/lang/Class;", type.getOop());
				ops.putInt(field, "modifiers", Modifier.eraseField(fn.getModifiers()));
				ops.putReference(field, "signature", "Ljava/lang/String;", pool.intern(fn.getSignature()));
				ops.putReference(field, "annotations", "[B", readFieldAnnotations(fn));
				result.setReference(j, field);
			}
			ctx.setResult(result);
			return Result.ABORT;
		});
		vmi.setInvoker(jlc, "getInterfaces0", "()[Ljava/lang/Class;", ctx -> {
			JavaClass _this = ctx.getLocals().<JavaValue<JavaClass>>loadReference(0).getValue();
			InstanceJavaClass[] interfaces = _this.getInterfaces();
			ArrayValue types = vm.getHelper().convertClasses(interfaces);
			ctx.setResult(types);
			return Result.ABORT;
		});
		vmi.setInvoker(jlc, "getEnclosingMethod0", "()[Ljava/lang/Object;", ctx -> {
			JavaClass klasas = ((JavaValue<JavaClass>) ctx.getLocals().loadReference(0)).getValue();
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
			JavaClass klasas = ((JavaValue<JavaClass>) ctx.getLocals().loadReference(0)).getValue();
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
			JavaClass klasas = ((JavaValue<JavaClass>) ctx.getLocals().loadReference(0)).getValue();
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
			ObjectValue value = locals.loadReference(1);
			if (value.isNull()) {
				ctx.setResult(0);
			} else {
				JavaClass klass = value.getJavaClass();
				JavaClass _this = locals.<JavaValue<JavaClass>>loadReference(0).getValue();
				ctx.setResult(_this.isAssignableFrom(klass) ? 1 : 0);
			}
			return Result.ABORT;
		});
		vmi.setInvoker(jlc, "getComponentType", "()Ljava/lang/Class;", ctx -> {
			JavaClass type = ctx.getLocals().<JavaValue<JavaClass>>loadReference(0).getValue().getComponentType();
			ctx.setResult(type == null ? vm.getMemoryManager().nullValue() : type.getOop());
			return Result.ABORT;
		});
		vmi.setInvoker(jlc, "getProtectionDomain0", "()Ljava/security/ProtectionDomain;", ctx -> {
			InstanceValue _this = ctx.getLocals().loadReference(0);
			JavaClass cl = ((JavaValue<JavaClass>) _this).getValue();
			if (cl.isPrimitive()) {
				ctx.setResult(vm.getMemoryManager().nullValue());
			} else {
				ctx.setResult(vm.getTrustedOperations().getReference(_this, PROTECTION_DOMAIN, "Ljava/security/ProtectionDomain;"));
			}
			return Result.ABORT;
		});
		vmi.setInvoker(jlc, "getRawAnnotations", "()[B", ctx -> {
			JavaClass _this = ctx.getLocals().<JavaValue<JavaClass>>loadReference(0).getValue();
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
			JavaClass _this = ctx.getLocals().<JavaValue<JavaClass>>loadReference(0).getValue();
			VMHelper helper = vm.getHelper();
			if (!(_this instanceof InstanceJavaClass)) {
				ctx.setResult(helper.emptyArray(jlc));
			} else {
				List<InnerClassNode> declaredClasses = ((InstanceJavaClass) _this).getNode().innerClasses;
				ObjectValue loader = _this.getClassLoader();
				ArrayValue array = helper.newArray(jlc, declaredClasses.size());
				for (int i = 0; i < declaredClasses.size(); i++) {
					array.setReference(i, helper.findClass(loader, declaredClasses.get(i).name, false).getOop());
				}
				ctx.setResult(array);
			}
			return Result.ABORT;
		});

		InstanceJavaClass cpClass = symbols.reflect_ConstantPool();
		vmi.setInvoker(jlc, "getConstantPool", "()" + cpClass.getDescriptor(), ctx -> {
			InstanceValue _this = ctx.getLocals().loadReference(0);
			cpClass.initialize();
			InstanceValue instance = vm.getMemoryManager().newInstance(cpClass);
			vm.getTrustedOperations().putReference(instance, "constantPoolOop", "Ljava/lang/Object;", _this);
			ctx.setResult(instance);
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
			array.setReference(i, helper.findClass(loader, exceptions.get(i), false).getOop());
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
