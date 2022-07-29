package dev.xdark.ssvm.value;

import dev.xdark.ssvm.memory.management.MemoryManager;
import dev.xdark.ssvm.memory.allocation.MemoryBlock;
import dev.xdark.ssvm.mirror.InstanceJavaClass;

/**
 * Base implementation of instance value.
 *
 * @author xDark
 */
public class SimpleInstanceValue extends SimpleObjectValue implements InstanceValue {

	private boolean initialized;

	/**
	 * @param memoryManager Memory manager.
	 * @param memory Object data.
	 */
	public SimpleInstanceValue(MemoryManager memoryManager, MemoryBlock memory) {
		super(memoryManager, memory);
	}

	@Override
	public final InstanceJavaClass getJavaClass() {
		return (InstanceJavaClass) super.getJavaClass();
	}

	@Override
	public boolean isUninitialized() {
		return !initialized;
	}

	@Override
	public ObjectValue getValue(String field, String desc) {
		return getMemoryManager().readValue(this, getFieldOffset(field, desc));
	}

	@Override
	public void setValue(String field, String desc, ObjectValue value) {
		getMemoryManager().writeValue(this, getFieldOffset(field, desc), value);
	}

	@Override
	public void initialize() {
		initialized = true;
	}

	@Override
	public long getFieldOffset(String name, String desc) {
		long offset = getJavaClass().getVirtualFieldOffsetRecursively(name, desc);
		return offset == -1L ? -1L : getMemoryManager().valueBaseOffset(this) + offset;
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
