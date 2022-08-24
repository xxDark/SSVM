package dev.xdark.ssvm.value;

import dev.xdark.ssvm.memory.allocation.MemoryBlock;
import dev.xdark.ssvm.memory.allocation.MemoryData;
import dev.xdark.ssvm.mirror.JavaClass;

/**
 * Represents VM object value.
 *
 * @author xDark
 */
public interface ObjectValue extends Value, Synchronizable {

	/**
	 * Returns object class.
	 *
	 * @return object class.
	 */
	JavaClass getJavaClass();

	/**
	 * Returns object memory block.
	 *
	 * @return memory block.
	 */
	MemoryBlock getMemory();

	/**
	 * Returns whether this as uninitialized value or not.
	 *
	 * @return {@code true} if the value is uninitialized.
	 */
	boolean isUninitialized();

	/**
	 * Returns whether this value is null or not.
	 *
	 * @return {@code true} if the value is null.
	 */
	boolean isNull();

	/**
	 * Returns object memory data.
	 *
	 * @return memory data.
	 */
	default MemoryData getData() {
		return getMemory().getData();
	}
}
