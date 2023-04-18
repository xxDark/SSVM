package dev.xdark.ssvm.memory.allocation;

/**
 * Basic implementation for a memory block.
 *
 * @author xDark
 */
public class SimpleMemoryBlock implements MemoryBlock {
	private final long address;
	private final MemoryData data;
	private final boolean heap;

	public SimpleMemoryBlock(long address, MemoryData data, boolean heap) {
		this.address = address;
		this.data = data;
		this.heap = heap;
	}

	@Override
	public long getAddress() {
		return address;
	}

	@Override
	public MemoryData getData() {
		return data;
	}

	@Override
	public boolean isHeap() {
		return heap;
	}
}
