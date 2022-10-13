package dev.xdark.ssvm;

import dev.xdark.ssvm.mirror.type.InstanceClass;
import dev.xdark.ssvm.mirror.type.JavaClass;
import dev.xdark.ssvm.mirror.member.JavaField;
import dev.xdark.ssvm.mirror.member.JavaMethod;
import dev.xdark.ssvm.operation.ExceptionOperations;
import dev.xdark.ssvm.symbol.Symbols;
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
	private final Symbols symbols;
	private final ExceptionOperations exceptionOperations;

	public LinkResolver(Symbols symbols, ExceptionOperations exceptionOperations) {
		this.symbols = symbols;
		this.exceptionOperations = exceptionOperations;
	}

	public JavaMethod resolveMethod(JavaClass klass, String name, String desc, boolean requireMethodRef) {
		if (requireMethodRef && klass.isInterface()) {
			exceptionOperations.throwException(symbols.java_lang_IncompatibleClassChangeError(), "Found interface " + klass.getName() + ", but class was expected");
		}
		return doResolveMethod(klass, name, desc);
	}

	public JavaMethod resolveVirtualMethod(JavaClass klass, JavaClass current, String name, String desc) {
		JavaMethod method = resolveMethod(klass, name, desc, true);
		if (klass.isInterface() && (method.getModifiers() & ACC_PRIVATE) != 0) {
			exceptionOperations.throwException(symbols.java_lang_IncompatibleClassChangeError(), "private interface method requires invokespecial, not invokevirtual: method " + formatMethod(klass, name, desc) + ", caller-class: " + (current == null ? "<NULL>" : current.getInternalName()));
		}
		if ((method.getModifiers() & ACC_STATIC) != 0) {
			exceptionOperations.throwException(symbols.java_lang_IncompatibleClassChangeError(), "Expected non-static method " + formatMethod(klass, name, desc));
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
			exceptionOperations.throwException(symbols.java_lang_IncompatibleClassChangeError(), "Found class " + klass.getName() + ", but interface was expected");
		}
		JavaMethod method = doResolveMethod(klass, name, desc);
		if (nonStatic && (method.getModifiers() & ACC_STATIC) != 0) {
			exceptionOperations.throwException(symbols.java_lang_IncompatibleClassChangeError(), "Expected instance not static method " + formatMethod(klass, name, desc));
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
			exceptionOperations.throwException(symbols.java_lang_IncompatibleClassChangeError(), "Expected static method " + formatMethod(klass, name, desc));
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
			exceptionOperations.throwException(symbols.java_lang_NoSuchMethodError(), klass.getName() + ": method " + name + desc + " not found");
		}
		if ((method.getModifiers() & ACC_STATIC) != 0) {
			exceptionOperations.throwException(symbols.java_lang_IncompatibleClassChangeError(), "Expected non-static method " + formatMethod(klass, name, desc));
		}
		return method;
	}

	public JavaField resolveStaticField(InstanceClass klass, String name, String desc) {
		InstanceClass current = klass;
		while (klass != null) {
			JavaField field = klass.getField(name, desc);
			if (field != null) {
				return field;
			}
			klass = klass.getSuperClass();
		}
		JavaField field = slowResolveStaticField(name, desc, current);
		if (field != null) {
			return field;
		}
		exceptionOperations.throwException(symbols.java_lang_NoSuchFieldError(), name);
		return null;
	}

	public JavaField resolveVirtualField(InstanceClass current, String name, String desc) {
		JavaField field = null;
		while (current != null) {
			if ((field = current.getField(name, desc)) != null) {
				break;
			}
			current = current.getSuperClass();
		}
		if (field != null) {
			return field;
		}
		exceptionOperations.throwException(symbols.java_lang_NoSuchFieldError(), name);
		return null;
	}

	private JavaMethod doResolveMethod(JavaClass klass, String name, String desc) {
		JavaMethod method = lookupMethodInClasses(klass, name, desc, false);
		if (method == null && !klass.isArray()) {
			method = lookupMethodInInterfaces((InstanceClass) klass, name, desc);
		}
		if (method == null) {
			exceptionOperations.throwException(symbols.java_lang_NoSuchMethodError(), formatMethod(klass, name, desc));
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
			InstanceClass jc = (InstanceClass) klass;
			method = jc.getMethod(name, desc);
		}
		return method;
	}

	private JavaMethod lookupMethodInInterfaces(InstanceClass klass, String name, String desc) {
		Deque<InstanceClass> deque = new ArrayDeque<>();
		InstanceClass current = klass;
		do {
			deque.addAll(Arrays.asList(current.getInterfaces()));
			while ((klass = deque.poll()) != null) {
				JavaMethod method = klass.getMethod(name, desc);
				if (method != null) {
					return method;
				}
				deque.addAll(Arrays.asList(klass.getInterfaces()));
			}
		} while ((current = current.getSuperClass()) != null);
		return null;
	}

	private JavaMethod uncachedLookupMethod(JavaClass klass, String name, String desc) {
		if (klass.isArray()) {
			return uncachedLookupMethod(symbols.java_lang_Object(), name, desc);
		}
		InstanceClass jc = (InstanceClass) klass;
		while (jc != null) {
			JavaMethod method = jc.getMethod(name, desc);
			if (method != null) {
				return method;
			}
			jc = jc.getSuperClass();
		}
		return null;
	}

	private static JavaField slowResolveStaticField(String name, String desc, InstanceClass current) {
		Deque<InstanceClass> deque = new ArrayDeque<>();
		deque.push(current);
		while ((current = deque.poll()) != null) {
			JavaField field = current.getField(name, desc);
			if (field != null) {
				return field;
			}
			InstanceClass parent = current.getSuperClass();
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
