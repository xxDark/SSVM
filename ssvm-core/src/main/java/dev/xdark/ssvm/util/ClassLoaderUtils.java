package dev.xdark.ssvm.util;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.classloading.ClassLoaders;
import dev.xdark.ssvm.execution.Locals;
import dev.xdark.ssvm.mirror.member.JavaMethod;
import dev.xdark.ssvm.mirror.type.InstanceClass;
import dev.xdark.ssvm.operation.VMOperations;
import dev.xdark.ssvm.value.InstanceValue;

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
}
