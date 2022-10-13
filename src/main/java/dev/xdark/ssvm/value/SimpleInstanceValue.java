package dev.xdark.ssvm.value;

import dev.xdark.ssvm.memory.allocation.MemoryBlock;
import dev.xdark.ssvm.memory.management.MemoryManager;
import dev.xdark.ssvm.mirror.type.InstanceClass;

/**
 * Base implementation of instance value.
 *
 * @author xDark
 */
public class SimpleInstanceValue extends SimpleObjectValue implements InstanceValue {

	/**
	 * @param memoryManager Memory manager.
	 * @param memory Object data.
	 */
	public SimpleInstanceValue(MemoryManager memoryManager, MemoryBlock memory) {
		super(memoryManager, memory);
	}

	@Override
	public final InstanceClass getJavaClass() {
		return (InstanceClass) super.getJavaClass();
	}

	@Override
	public boolean isNull() {
		return false;
	}

	@Override
	public String toString() {
		return getJavaClass().getInternalName();
	}
}
