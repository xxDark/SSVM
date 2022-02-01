package dev.xdark.ssvm.memory;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.nio.ByteBuffer;

/**
 * Simple memory block implementation.
 */
@Getter
@RequiredArgsConstructor
public class SimpleMemory implements Memory {

	private final MemoryManager memoryManager;
	private final ByteBuffer data;
	private final long address;
	private final boolean isDirect;

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Memory)) return false;

		Memory memory = (Memory) o;

		return address == memory.getAddress();
	}

	@Override
	public int hashCode() {
		return Long.hashCode(address);
	}
}
