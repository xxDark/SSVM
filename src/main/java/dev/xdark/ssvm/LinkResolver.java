package dev.xdark.ssvm;

import dev.xdark.ssvm.asm.Modifier;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.mirror.JavaClass;
import dev.xdark.ssvm.mirror.JavaField;
import dev.xdark.ssvm.mirror.JavaMethod;
import dev.xdark.ssvm.symbol.VMSymbols;
import dev.xdark.ssvm.util.VMHelper;
import dev.xdark.ssvm.value.ArrayValue;
import dev.xdark.ssvm.value.ObjectValue;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;

import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;

/**
 * Linking code.
 *
 * @author xDark
 */
public final class LinkResolver {
	private final VMHelper helper;
	private final VMSymbols symbols;
	private final boolean trusted;

	/**
	 * @param vm      VM instance.
	 * @param trusted Wthether the link resolver is truted.
	 */
	public LinkResolver(VirtualMachine vm, boolean trusted) {
		helper = vm.getHelper();
		symbols = vm.getSymbols();
		this.trusted = trusted;
	}

	public JavaMethod resolveMethod(JavaClass klass, String name, String desc, boolean requireMethodRef) {
		if (requireMethodRef && klass.isInterface()) {
			helper.throwException(symbols.java_lang_IncompatibleClassChangeError(), "Found interface " + klass.getName() + ", but class was expected");
		}
		return doResolveMethod(klass, name, desc);
	}

	public JavaMethod resolveVirtualMethod(JavaClass klass, JavaClass current, String name, String desc) {
		JavaMethod method = resolveMethod(klass, name, desc, true);
		if (klass.isInterface() && (method.getModifiers() & ACC_PRIVATE) != 0) {
			helper.throwException(symbols.java_lang_IncompatibleClassChangeError(), "private interface method requires invokespecial, not invokevirtual: method " + formatMethod(klass, name, desc) + ", caller-class: " + (current == null ? "<NULL>" : current.getInternalName()));
		}
		if ((method.getModifiers() & ACC_STATIC) != 0) {
			helper.throwException(symbols.java_lang_IncompatibleClassChangeError(), "Expected non-static method " + formatMethod(klass, name, desc));
		}
		return method;
	}

	public JavaMethod resolveVirtualMethod(JavaClass klass, ObjectValue value, String name, String desc) {
		JavaClass current;
		if (value instanceof ArrayValue) {
			current = symbols.java_lang_Object();
		} else {
			current = value.getJavaClass();
		}
		return resolveVirtualMethod(klass, current, name, desc);
	}

	public JavaMethod resolveVirtualMethod(ObjectValue value, String name, String desc) {
		JavaClass current;
		if (value instanceof ArrayValue) {
			current = symbols.java_lang_Object();
		} else {
			current = value.getJavaClass();
		}
		return resolveVirtualMethod(current, current, name, desc);
	}

	public JavaMethod resolveInterfaceMethod(JavaClass klass, String name, String desc, boolean nonStatic) {
		if (!klass.isInterface()) {
			helper.throwException(symbols.java_lang_IncompatibleClassChangeError(), "Found class " + klass.getName() + ", but interface was expected");
		}
		JavaMethod method = doResolveMethod(klass, name, desc);
		if (nonStatic && (method.getModifiers() & ACC_STATIC) != 0) {
			helper.throwException(symbols.java_lang_IncompatibleClassChangeError(), "Expected instance not static method " + formatMethod(klass, name, desc));
		}
		return method;
	}

	public JavaMethod resolveStaticMethod(JavaClass klass, String name, String desc) {
		JavaMethod method;
		if (klass.isInterface()) {
			method = resolveInterfaceMethod(klass, name, desc, false);
		} else {
			method = resolveMethod(klass, name, desc, false);
		}
		if ((method.getModifiers() & ACC_STATIC) == 0) {
			helper.throwException(symbols.java_lang_IncompatibleClassChangeError(), "Expected static method " + formatMethod(klass, name, desc));
		}
		return method;
	}

	public JavaMethod resolveSpecialMethod(JavaClass klass, String name, String desc) {
		JavaMethod method;
		if (klass.isInterface()) {
			method = resolveInterfaceMethod(klass, name, desc, true);
		} else {
			method = resolveMethod(klass, name, desc, false);
		}
		if (method.isConstructor() && klass != method.getOwner()) {
			helper.throwException(symbols.java_lang_NoSuchMethodError(), klass.getName() + ": method " + name + desc + " not found");
		}
		if ((method.getModifiers() & ACC_STATIC) != 0) {
			helper.throwException(symbols.java_lang_IncompatibleClassChangeError(), "Expected non-static method " + formatMethod(klass, name, desc));
		}
		return method;
	}

	public JavaField resolveStaticField(InstanceJavaClass klass, String name, String desc) {
		InstanceJavaClass current = klass;
		while (klass != null) {
			JavaField field = klass.getStaticField(name, desc);
			if (field != null) {
				return field;
			}
			klass = klass.getSuperClass();
		}
		JavaField field = slowResolveStaticField(name, desc, current);
		if (field != null && (trusted || !Modifier.isHiddenMember(field.getModifiers()))) {
			return field;
		}
		helper.throwException(symbols.java_lang_NoSuchFieldError(), name);
		return null;
	}

	public JavaField resolveVirtualField(InstanceJavaClass klass, InstanceJavaClass current, String name, String desc) {
		JavaField field = klass.getVirtualField(name, desc);
		if (field == null) {
			while (current != null) {
				if ((field = current.getVirtualField(name, desc)) != null) {
					break;
				}
				current = current.getSuperClass();
			}
		}
		if (field != null && (trusted || !Modifier.isHiddenMember(field.getModifiers()))) {
			return field;
		}
		helper.throwException(symbols.java_lang_NoSuchFieldError(), name);
		return null;
	}

	private JavaMethod doResolveMethod(JavaClass klass, String name, String desc) {
		JavaMethod method = lookupMethodInClasses(klass, name, desc, false);
		if (method == null && !klass.isArray()) {
			method = lookupMethodInInterfaces((InstanceJavaClass) klass, name, desc);
		}
		if (method == null) {
			helper.throwException(symbols.java_lang_NoSuchMethodError(), formatMethod(klass, name, desc));
		}
		return method;
	}

	private JavaMethod lookupMethodInClasses(JavaClass klass, String name, String desc, boolean inMethodResolve) {
		JavaMethod method = uncachedLookupMethod(klass, name, desc);
		if (klass.isArray()) {
			return method;
		}
		int acc;
		if (inMethodResolve &&
			method != null &&
			klass.isInterface() &&
			(((acc = method.getModifiers()) & ACC_STATIC) != 0 || (acc & ACC_PUBLIC) == 0) &&
			method.getOwner() == symbols.java_lang_Object()) {
			method = null;
		}
		if (method == null) {
			InstanceJavaClass jc = (InstanceJavaClass) klass;
			method = jc.getMethod(name, desc);
		}
		return method;
	}

	private JavaMethod lookupMethodInInterfaces(InstanceJavaClass klass, String name, String desc) {
		Deque<InstanceJavaClass> deque = new ArrayDeque<>();
		InstanceJavaClass current = klass;
		do {
			deque.addAll(Arrays.asList(current.getInterfaces()));
			while ((klass = deque.poll()) != null) {
				JavaMethod method = klass.getMethod(name, desc);
				if (method != null) {
					return method;
				}
				deque.addAll(Arrays.asList(klass.getInterfaces()));
			}
		} while ((current = current.getSuperclassWithoutResolving()) != null);
		return null;
	}

	private JavaMethod uncachedLookupMethod(JavaClass klass, String name, String desc) {
		if (klass.isArray()) {
			return uncachedLookupMethod(symbols.java_lang_Object(), name, desc);
		}
		InstanceJavaClass jc = (InstanceJavaClass) klass;
		while (jc != null) {
			JavaMethod method = jc.getMethod(name, desc);
			if (method != null) {
				return method;
			}
			jc = jc.getSuperclassWithoutResolving();
		}
		return null;
	}

	private static JavaField slowResolveStaticField(String name, String desc, InstanceJavaClass current) {
		Deque<InstanceJavaClass> deque = new ArrayDeque<>();
		deque.push(current);
		while ((current = deque.poll()) != null) {
			JavaField field = current.getStaticField(name, desc);
			if (field != null) {
				return field;
			}
			InstanceJavaClass parent = current.getSuperClass();
			if (parent != null) {
				deque.push(parent);
			}
			deque.addAll(Arrays.asList(current.getInterfaces()));
		}
		return null;
	}

	private static String formatMethod(JavaClass klass, String name, String desc) {
		return klass.getName() + '.' + name + desc;
	}
}
