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
		if (!(o instanceof SimpleMemory)) return false;

		SimpleMemory memory = (SimpleMemory) o;

		return address == memory.address;
	}

	@Override
	public int hashCode() {
		return Long.hashCode(address);
	}
}
