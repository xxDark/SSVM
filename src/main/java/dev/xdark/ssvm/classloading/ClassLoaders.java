package dev.xdark.ssvm.classloading;

import dev.xdark.ssvm.value.InstanceValue;
import dev.xdark.ssvm.value.ObjectValue;

import java.util.Collection;

/**
 * Class loaders storage.
 *
 * @author xDark
 */
public interface ClassLoaders {

	/**
	 * Registers new class loader.
	 *
	 * @param classLoader
	 * 		Class loader to register.
	 */
	void register(InstanceValue classLoader);

	/**
	 * Sets class loader data.
	 *
	 * @param classLoader
	 * 		Class loader to set data for.
	 *
	 * @return set data.
	 *
	 * @throws IllegalStateException
	 * 		If data is already set.
	 */
	ClassLoaderData setClassLoaderData(ObjectValue classLoader);

	/**
	 * @param classLoader
	 * 		Class loader to get data for.
	 *
	 * @return class sotrage or {@code null}, if unset.
	 */
	ClassLoaderData getClassLoaderData(ObjectValue classLoader);

	/**
	 * @return list of all registered laoders.
	 */
	Collection<InstanceValue> getAll();
}
