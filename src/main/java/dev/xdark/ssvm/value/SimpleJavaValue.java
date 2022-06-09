package dev.xdark.ssvm.value;

import dev.xdark.ssvm.memory.management.MemoryManager;
import dev.xdark.ssvm.memory.allocation.MemoryBlock;

/**
 * Base implementation of Java value wrapper.
 *
 * @param <V> Type of Java object.
 * @author xDark
 */
public final class SimpleJavaValue<V> extends SimpleInstanceValue implements JavaValue<V> {

	private final V value;
	private boolean wide;

	/**
	 * @param memoryManager Memory manager.
	 * @param memory        Object data.
	 * @param value         Java value.
	 */
	public SimpleJavaValue(MemoryManager memoryManager, MemoryBlock memory, V value) {
		super(memoryManager, memory);
		this.value = value;
	}

	@Override
	public V getValue() {
		return value;
	}

	@Override
	public boolean isWide() {
		return wide;
	}

	@Override
	public void setWide(boolean wide) {
		this.wide = wide;
	}
}
