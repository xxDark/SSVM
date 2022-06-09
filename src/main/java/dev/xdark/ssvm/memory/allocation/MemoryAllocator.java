package dev.xdark.ssvm.memory.allocation;

import java.nio.ByteOrder;

/**
 * Memory allocation interface.
 *
 * @author xDark
 */
public interface MemoryAllocator {

	/**
	 * @return empty memory block
	 * at the address of {@literal  0}.
	 */
	MemoryBlock emptyHeapBlock();

	/**
	 * @return empty memory block
	 * at the address of {@literal  0}.
	 */
	MemoryBlock emptyDirectBlock();

	/**
	 * Locates a memory block.
	 *
	 * @param address Possible block address.
	 * @return memory block or {@code null},
	 * if not found.
	 */
	MemoryBlock findHeapBlock(long address);

	/**
	 * Locates a memory block.
	 *
	 * @param address Possible block address.
	 * @return memory block or {@code null},
	 * if not found.
	 */
	MemoryBlock findDirectBlock(long address);

	/**
	 * Allocates heap block.
	 *
	 * @param bytes Size of the block.
	 * @return allocated block or {@code null},
	 * if allocation failed.
	 */
	MemoryBlock allocateHeap(long bytes);

	/**
	 * Allocates direct block.
	 *
	 * @param bytes Size of the block.
	 * @return allocated block or {@code null},
	 * if allocation failed.
	 */
	MemoryBlock allocateDirect(long bytes);

	/**
	 * Reallocates {@literal direct} memory.
	 *
	 * @param address Address of the block.
	 * @param bytes   How much memory to reallocate.
	 * @return Block with reallocated memory or {@code null},
	 * if reallocation failed.
	 */
	MemoryBlock reallocateDirect(long address, long bytes);

	/**
	 * Deallocates heap block.
	 *
	 * @param address Block address to deallocate.
	 * @return {@code true} if block was deallocated,
	 * {@code false} otherwise.
	 */
	boolean freeHeap(long address);

	/**
	 * Deallocates off-heap block.
	 *
	 * @param address Block address to deallocate.
	 * @return {@code true} if block was deallocated,
	 * {@code false} otherwise.
	 */
	boolean freeDirect(long address);

	/**
	 * Returns byte order of memory.
	 *
	 * @return byte order of memory.
	 */
	ByteOrder getByteOrder();

	/**
	 * Returns address size.
	 *
	 * @return address size.
	 */
	int addressSize();

	/**
	 * Returns page size.
	 *
	 * @return page size.
	 */
	int pageSize();

	/**
	 * @return current statistics of the allocator.
	 * {@code null} may be returned to indicate
	 * that this allocator does not support statistics.
	 */
	MemoryAllocatorStatistics dumpStatistics();

	/**
	 * @return real-time statistics of the allocator.
	 * {@code null} may be returned to indicate
	 * that this allocator does not support statistics.
	 */
	MemoryAllocatorStatistics liveStatistics();
}
