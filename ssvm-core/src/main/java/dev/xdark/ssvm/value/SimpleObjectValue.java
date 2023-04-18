package dev.xdark.ssvm.value;

import dev.xdark.ssvm.memory.management.MemoryManager;
import dev.xdark.ssvm.memory.allocation.MemoryBlock;
import dev.xdark.ssvm.mirror.type.JavaClass;

/**
 * Represents VM object value.
 *
 * @author xDark
 */
public abstract class SimpleObjectValue implements ObjectValue {

	private final MemoryManager memoryManager;
	protected final MemoryBlock memory;

	/**
	 * @param memoryManager Memory manager.
	 * @param memory        Object data.
	 */
	public SimpleObjectValue(MemoryManager memoryManager, MemoryBlock memory) {
		this.memoryManager = memoryManager;
		this.memory = memory;
	}

	@Override
	public long asLong() {
		return memory.getData().readLong(0L);
	}

	@Override
	public int asInt() {
		return memory.getData().readInt(0L);
	}

	@Override
	public char asChar() {
		return memory.getData().readChar(0L);
	}

	@Override
	public short asShort() {
		return memory.getData().readShort(0L);
	}

	@Override
	public byte asByte() {
		return memory.getData().readByte(0L);
	}

	@Override
	public boolean asBoolean() {
		return asByte() != 0;
	}

	@Override
	public JavaClass getJavaClass() {
		return getMemoryManager().readClass(this);
	}

	@Override
	public MemoryBlock getMemory() {
		return memory;
	}

	protected MemoryManager getMemoryManager() {
		return memoryManager;
	}
}
