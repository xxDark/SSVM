package dev.xdark.ssvm.memory.allocation;

/**
 * Statistics dump of the allocator.
 *
 * @author xDark
 */
public interface MemoryAllocatorStatistics {

	/**
	 * @return the amount of free memory.
	 */
	long freeSpace();

	/**
	 * @return the amount of memory in use.
	 */
	long usedSpace();

	/**
	 * @return maximum amount of memory
	 * the allocator can use.
	 */
	long maxSpace();

	/**
	 * @return total amount of memory
	 * the allocator can use.
	 */
	long totalSpace();
}
