package dev.xdark.ssvm.classloading;

import dev.xdark.ssvm.mirror.InstanceJavaClass;

import java.util.Collection;

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

	/**
	 * @return a collection of all classes.
	 */
	Collection<InstanceJavaClass> getAll();

	/**
	 * Claims another class loader data.
	 *
	 * @param classLoaderData
	 * 		Class loader data to claim.
	 */
	void claim(ClassLoaderData classLoaderData);
}
