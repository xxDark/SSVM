package dev.xdark.ssvm.classloading;

import dev.xdark.ssvm.mirror.JavaClass;

import java.util.HashMap;
import java.util.Map;

/**
 * Holds all classes loaded by the loader.
 *
 * @author xDark
 */
public final class ClassLoaderData {

	private final Map<String, JavaClass> table = new HashMap<>();

	/**
	 * Returns a class based off it's name.
	 *
	 * @param name
	 * 		Name of the class.
	 *
	 * @return class.
	 */
	public JavaClass getClass(String name) {
		return table.get(name);
	}

	/**
	 * Attempts to register a class.
	 *
	 * @param jc
	 * 		Class to register.
	 *
	 * @throws IllegalStateException
	 * 		If the class with the name of {@code jc} is already linked.
	 */
	public void linkClass(JavaClass jc) {
		String name = jc.getInternalName();
		if (table.putIfAbsent(name, jc) != null) {
			throw new IllegalStateException(name);
		}
	}

	/**
	 * Registers a class.
	 * This method will REPLACE existing class.
	 *
	 * @param jc
	 * 		Class to register.
	 */
	public void forceLinkClass(JavaClass jc) {
		table.put(jc.getInternalName(), jc);
	}
}
