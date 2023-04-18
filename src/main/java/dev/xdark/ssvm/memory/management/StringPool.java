package dev.xdark.ssvm.memory.management;

import dev.xdark.ssvm.value.InstanceValue;
import dev.xdark.ssvm.value.ObjectValue;

/**
 * String pool. Only used for {@link String#intern()}.
 *
 * @author xDark
 */
public interface StringPool {

	/**
	 * Pools string value.
	 *
	 * @param value Value to pool.
	 * @return interned string value.
	 */
	ObjectValue intern(String value);

	/**
	 * Pools string value.
	 *
	 * @param value Value to pool.
	 * @return interned string value.
	 */
	InstanceValue intern(InstanceValue value);

	/**
	 * Gets interned string from pool if available.
	 *
	 * @param str String to get.
	 * @return interned string value or {@code null},
	 * if not found.
	 */
	InstanceValue getIfPresent(String str);
}
