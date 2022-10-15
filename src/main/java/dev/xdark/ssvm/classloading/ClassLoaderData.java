package dev.xdark.ssvm.classloading;

import dev.xdark.ssvm.mirror.type.InstanceClass;
import dev.xdark.ssvm.util.AutoCloseableLock;

import java.util.Collection;
import java.util.function.Consumer;

/**
 * Holds all classes loaded by the loader.
 *
 * @author xDark
 */
public interface ClassLoaderData {

	/**
	 * Returns a class based off it's name.
	 *
	 * @param name Name of the class.
	 * @return class.
	 */
	InstanceClass getClass(String name);


	/**
	 * Attempts to register a class.
	 *
	 * @param jc Class to register.
	 * @return {@code true} if class has been successfully linked.
	 */
	boolean linkClass(InstanceClass jc);

	/**
	 * @return Acquired lock.
	 */
	AutoCloseableLock lock();

	/**
	 * Returns a collection of all classes.
	 * The returned collection is unmodifiable and not thread-safe,
	 * this method is suggested to be used in conjunction with
	 * {@link ClassLoaderData#lock()}.
	 *
	 * @return A collection of all classes.
	 * @see AutoCloseableLock
	 */
	Collection<InstanceClass> all();
}
