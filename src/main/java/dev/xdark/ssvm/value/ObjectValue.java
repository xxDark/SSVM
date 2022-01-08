package dev.xdark.ssvm.value;

import dev.xdark.ssvm.memory.Memory;
import dev.xdark.ssvm.memory.MemoryManager;
import dev.xdark.ssvm.mirror.FieldInfo;
import dev.xdark.ssvm.mirror.JavaClass;

/**
 * Represents VM object value.
 *
 * @author xDark
 */
public class ObjectValue implements Value {

	private final Memory memory;

	/**
	 * @param memory
	 * 		Object data.
	 */
	public ObjectValue(Memory memory) {
		this.memory = memory;
	}

	@Override
	public <T> T as(Class<T> type) {
		throw new IllegalStateException(type.toString());
	}

	@Override
	public boolean isWide() {
		return false;
	}

	@Override
	public boolean isUninitialized() {
		return false;
	}

	@Override
	public boolean isNull() {
		return false;
	}

	/**
	 * Returns object class.
	 *
	 * @return object class.
	 */
	public JavaClass getJavaClass() {
		return getMemoryManager().readClass(this);
	}

	/**
	 * Returns object data.
	 *
	 * @return object data.
	 */
	public Memory getMemory() {
		return memory;
	}

	protected MemoryManager getMemoryManager() {
		return memory.getMemoryManager();
	}
}
