package dev.xdark.ssvm.util;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.classloading.ClassLoaders;
import dev.xdark.ssvm.execution.Locals;
import dev.xdark.ssvm.mirror.member.JavaMethod;
import dev.xdark.ssvm.mirror.type.InstanceClass;
import dev.xdark.ssvm.operation.VMOperations;
import dev.xdark.ssvm.value.InstanceValue;
import dev.xdark.ssvm.value.ObjectValue;

/**
 * Various utilities for working with {@link ClassLoaders}.
 *
 * @author Matt Coley
 */
public class ClassLoaderUtils {
	/**
	 * @param vm VM to pull system classloader out of.
	 * @return Instance of system classloader.
	 */
	public static InstanceValue systemClassLoader(VirtualMachine vm) {
		InstanceClass java_lang_classLoader = vm.getSymbols().java_lang_ClassLoader();
		JavaMethod method = java_lang_classLoader.getMethod("getSystemClassLoader", "()Ljava/lang/ClassLoader;");
		Locals locals = vm.getThreadStorage().newLocals(method);
		VMOperations ops = vm.getOperations();
		return ops.checkNotNull(ops.invokeReference(method, locals));
	}

	/**
	 * @param vm VM to pull references from.
	 * @param loader Loader reference to check for class in.
	 * @param className Name of class to check.
	 * @return VM reference to {@link Class} instance for the class name.
	 */
	public static InstanceValue findClassInLoader(VirtualMachine vm, InstanceValue loader, String className) {
		try {
			InstanceClass java_lang_classLoader = vm.getSymbols().java_lang_ClassLoader();
			JavaMethod method = java_lang_classLoader.getMethod("loadClass", "(Ljava/lang/String;)Ljava/lang/Class;");
			VMOperations ops = vm.getOperations();
			Locals locals = vm.getThreadStorage().newLocals(method);
			locals.setReference(0, loader);
			locals.setReference(1, ops.newUtf8(className));
			return (InstanceValue) ops.invokeReference(method, locals);
		} catch (Throwable t) {
			// Expected, thrown when class not found
			return null;
		}
	}
}
