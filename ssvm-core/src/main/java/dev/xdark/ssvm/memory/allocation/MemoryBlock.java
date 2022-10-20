package dev.xdark.ssvm.memory.allocation;

/**
 * Allocated memory block.
 *
 * @author xDark
 */
public interface MemoryBlock {

	/**
	 * @return block address.
	 */
	long getAddress();

	/**
	 * @return block data.
	 */
	MemoryData getData();

	/**
	 * @return {@code true} if this block
	 * is a block of heap memory.
	 */
	boolean isHeap();
}
