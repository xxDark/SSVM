package dev.xdark.ssvm.classloading;

import dev.xdark.ssvm.mirror.type.InstanceClass;
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
	 * Sets class loader data.
	 *
	 * @param classLoader Class loader to set data for.
	 * @return set data.
	 * @throws IllegalStateException If data is already set.
	 */
	ClassLoaderData setClassLoaderData(ObjectValue classLoader);

	/**
	 * @param classLoader Class loader to get data for.
	 * @return class sotrage or {@code null}, if unset.
	 */
	ClassLoaderData getClassLoaderData(ObjectValue classLoader);

	/**
	 * @return list of all registered laoders.
	 */
	Collection<InstanceValue> getAll();

	/**
	 * Sets extra class data for a class.
	 * Used for JDK 17+.
	 *
	 * @param javaClass Class to set extra data for.
	 * @param classData Class data.
	 */
	void setClassData(InstanceClass javaClass, ObjectValue classData);

	/**
	 * Gets extra class data for a class.
	 * Used for JDK 17+.
	 *
	 * @param javaClass Class to get extra data for.
	 * @return extra data.
	 */
	ObjectValue getClassData(InstanceClass javaClass);
}
