package dev.xdark.ssvm.memory.allocation;

import java.nio.ByteOrder;

/**
 * Memory allocator that blocks on allocation request.
 *
 * @author xDark
 */
public class SynchronizedMemoryAllocator implements MemoryAllocator {
	protected final MemoryAllocator allocator;
	protected final Object mutex;

	/**
	 * @param allocator Backing allocator.
	 * @param mutex     Allocation mutex.
	 */
	public SynchronizedMemoryAllocator(MemoryAllocator allocator, Object mutex) {
		this.allocator = allocator;
		this.mutex = mutex;
	}

	/**
	 * @param allocator Backing allocator.
	 */
	public SynchronizedMemoryAllocator(MemoryAllocator allocator) {
		this.allocator = allocator;
		this.mutex = this;
	}

	@Override
	public MemoryBlock emptyHeapBlock() {
		return allocator.emptyHeapBlock();
	}

	@Override
	public MemoryBlock emptyDirectBlock() {
		return allocator.emptyDirectBlock();
	}

	@Override
	public MemoryBlock findHeapBlock(long address) {
		synchronized (mutex) {
			return allocator.findHeapBlock(address);
		}
	}

	@Override
	public MemoryBlock findDirectBlock(long address) {
		synchronized (mutex) {
			return allocator.findDirectBlock(address);
		}
	}

	@Override
	public MemoryBlock allocateHeap(long bytes) {
		synchronized (mutex) {
			return allocator.allocateHeap(bytes);
		}
	}

	@Override
	public MemoryBlock allocateDirect(long bytes) {
		synchronized (mutex) {
			return allocator.allocateDirect(bytes);
		}
	}

	@Override
	public MemoryBlock reallocateDirect(long address, long bytes) {
		synchronized (mutex) {
			return allocator.reallocateDirect(address, bytes);
		}
	}

	@Override
	public boolean freeHeap(long address) {
		synchronized (mutex) {
			return allocator.freeHeap(address);
		}
	}

	@Override
	public boolean freeDirect(long address) {
		synchronized (mutex) {
			return allocator.freeDirect(address);
		}
	}

	@Override
	public ByteOrder getByteOrder() {
		return allocator.getByteOrder();
	}

	@Override
	public int addressSize() {
		return allocator.addressSize();
	}

	@Override
	public int pageSize() {
		return allocator.pageSize();
	}

	@Override
	public MemoryAllocatorStatistics dumpStatistics() {
		synchronized (mutex) {
			return allocator.dumpStatistics();
		}
	}

	@Override
	public MemoryAllocatorStatistics liveStatistics() {
		synchronized (mutex) {
			return allocator.liveStatistics();
		}
	}
}
