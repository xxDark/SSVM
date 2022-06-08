package dev.xdark.ssvm.value;

import dev.xdark.ssvm.memory.Memory;
import dev.xdark.ssvm.mirror.JavaClass;

/**
 * Represents VM object value.
 *
 * @author xDark
 */
public interface ObjectValue extends Value, Synchronizable, ReferenceCounted {

	/**
	 * Returns object class.
	 *
	 * @return object class.
	 */
	JavaClass getJavaClass();

	/**
	 * Returns object data.
	 *
	 * @return object data.
	 */
	Memory getMemory();
}
