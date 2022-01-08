package dev.xdark.ssvm.memory;

import java.nio.ByteBuffer;

/**
 * Represents allocated memory block.
 *
 * @author xDark
 */
public final class Memory {

	private final MemoryManager memoryManager;
	private final ByteBuffer data;
	private final long address;
	private final boolean isDirect;

	/**
	 * @param memoryManager
	 * 		Manager which allocated this block.
	 * @param data
	 * 		Memory data.
	 * @param address
	 * 		Memory address.
	 * @param isDirect
	 * 		True if memory is direct.
	 */
	public Memory(MemoryManager memoryManager, ByteBuffer data, long address, boolean isDirect) {
		this.memoryManager = memoryManager;
		this.data = data;
		this.address = address;
		this.isDirect = isDirect;
	}

	/**
	 * Returns manager which allocated this block.
	 *
	 * @return manager which allocated this block.
	 */
	public MemoryManager getMemoryManager() {
		return memoryManager;
	}

	/**
	 * Returns memory data.
	 *
	 * @return memory data.
	 */
	public ByteBuffer getData() {
		return data;
	}

	/**
	 * Returns memory address.
	 *
	 * @return memory address.
	 */
	public long getAddress() {
		return address;
	}

	/**
	 * Returns whether memory is direct not.
	 *
	 * @return {@code true} if memory is direct, {@code false}
	 * otherwise.
	 */
	public boolean isDirect() {
		return isDirect;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Memory memory = (Memory) o;

		return address == memory.address;
	}

	@Override
	public int hashCode() {
		return Long.hashCode(address);
	}
}
