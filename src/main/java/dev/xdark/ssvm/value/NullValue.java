package dev.xdark.ssvm.value;

import dev.xdark.ssvm.execution.PanicException;
import dev.xdark.ssvm.memory.allocation.MemoryBlock;
import dev.xdark.ssvm.mirror.JavaClass;

/**
 * represents {@code null} value.
 *
 * @author xDark
 */
public final class NullValue implements ObjectValue {

	private final MemoryBlock memory;

	public NullValue(MemoryBlock memory) {
		this.memory = memory;
	}

	@Override
	public long asLong() {
		throw new PanicException("Segfault");
	}

	@Override
	public boolean isUninitialized() {
		throw new UnsupportedOperationException("Null value");
	}

	@Override
	public boolean isNull() {
		return true;
	}

	@Override
	public JavaClass getJavaClass() {
		throw new UnsupportedOperationException("Null value");
	}

	@Override
	public MemoryBlock getMemory() {
		return memory;
	}
}
