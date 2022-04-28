package dev.xdark.ssvm.classloading;

import dev.xdark.ssvm.mirror.InstanceJavaClass;

/**
 * Holds all classes loaded by the loader.
 *
 * @author xDark
 */
public interface ClassLoaderData {

	/**
	 * Returns a class based off it's name.
	 *
	 * @param name
	 * 		Name of the class.
	 *
	 * @return class.
	 */
	InstanceJavaClass getClass(String name);

	/**
	 * Checks if a class has been linked.
	 *
	 * @param name
	 * 		Name of the class.
	 *
	 * @return {@code true} when the class exists.
	 */
	boolean hasClass(String name);

	/**
	 * Unregisters a class by its name.
	 *
	 * @param name
	 * 		Name of the class.
	 *
	 * @return {@code true} when the class existed, and was successfully unlinked.
	 */
	boolean unlinkClass(String name);

	/**
	 * Attempts to register a class.
	 *
	 * @param jc
	 * 		Class to register.
	 *
	 * @throws IllegalStateException
	 * 		If the class with the name of {@code jc} is already linked.
	 */
	void linkClass(InstanceJavaClass jc);

	/**
	 * Registers a class.
	 * This method will REPLACE existing class.
	 *
	 * @param jc
	 * 		Class to register.
	 */
	void forceLinkClass(InstanceJavaClass jc);
}
