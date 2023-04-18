package dev.xdark.ssvm.memory.allocation;

import dev.xdark.ssvm.util.UnsafeUtil;

import java.nio.ByteOrder;

/**
 * Memory allocator base.
 *
 * @author xDark
 */
public abstract class AbstractMemoryAllocator implements MemoryAllocator {
	protected static final ByteOrder ORDER = ByteOrder.nativeOrder();
	protected static final int PAGE_SIZE = UnsafeUtil.get().pageSize();
	protected static final int ADDRESS_SIZE = 8;
	private final MemoryBlock emptyHeap = makeBlock(0L, 0L, true);
	private final MemoryBlock emptyDirect = makeBlock(0L, 0L, false);

	@Override
	public final MemoryBlock emptyHeapBlock() {
		return emptyHeap;
	}

	@Override
	public final MemoryBlock emptyDirectBlock() {
		return emptyDirect;
	}

	@Override
	public final ByteOrder getByteOrder() {
		return ORDER;
	}

	@Override
	public final int addressSize() {
		return ADDRESS_SIZE;
	}

	@Override
	public final int pageSize() {
		return PAGE_SIZE;
	}

	/**
	 * @param bytes Amount of bytes to allocate.
	 * @return {@code true} if the amount of bytes can be allocated.
	 */
	protected abstract boolean canAllocate(long bytes);

	/**
	 * Allocates new memory block.
	 *
	 * @param address Block address.
	 * @param bytes   Size of the block, in bytes.
	 * @param heap    Whether the block is a heap block.
	 * @return allocated block.
	 */
	protected abstract MemoryBlock makeBlock(long address, long bytes, boolean heap);
}
