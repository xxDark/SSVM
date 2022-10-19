package dev.xdark.ssvm.memory.allocation;

import dev.xdark.ssvm.execution.PanicException;
import dev.xdark.ssvm.threadlocal.ThreadLocalStorage;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Basic memory allocator that uses
 * navigable map to store memory blocks.
 *
 * @author xDark
 */
public class NavigableMemoryAllocator extends AbstractMemoryAllocator {

	private final NavigableMap<MemoryAddress, MemoryBlock> allocatedBlocks;

	/**
	 * @param allocatedBlocks Backing map.
	 */
	public NavigableMemoryAllocator(NavigableMap<MemoryAddress, MemoryBlock> allocatedBlocks) {
		this.allocatedBlocks = allocatedBlocks;
	}

	public NavigableMemoryAllocator() {
		this(new TreeMap<>());
	}

	@Override
	public MemoryBlock findHeapBlock(long address) {
		Map.Entry<MemoryAddress, MemoryBlock> block = findBlock(address, true);
		return block == null ? null : block.getValue();
	}

	@Override
	public MemoryBlock findDirectBlock(long address) {
		Map.Entry<MemoryAddress, MemoryBlock> block = findBlock(address, false);
		return block == null ? null : block.getValue();
	}

	@Override
	public MemoryBlock allocateHeap(long bytes) {
		return makeNewBlock(bytes, true);
	}

	@Override
	public MemoryBlock allocateDirect(long bytes) {
		return makeNewBlock(bytes, false);
	}

	@Override
	public MemoryBlock reallocateDirect(long address, long bytes) {
		MemoryAddress wrapper = ThreadLocalStorage.get().memoryAddress(address);
		MemoryBlock block = allocatedBlocks.remove(wrapper);
		if (block == null || block.isHeap()) {
			throw new PanicException("Segfault");
		}
		if (bytes == 0L) {
			return emptyDirectBlock();
		}
		MemoryData buffer = block.getData();
		long capacity = buffer.length();
		if (bytes < capacity) {
			// can we do that?
			// TODO verify
			throw new PanicException("Segfault");
		}
		MemoryBlock newBlock = makeNewBlock(bytes, false);
		if (newBlock == null) {
			return null;
		}
		MemoryData newBuffer = newBlock.getData();
		buffer.write(0L, newBuffer, 0L, buffer.length());
		return newBlock;
	}

	@Override
	public boolean freeHeap(long address) {
		Map.Entry<MemoryAddress, MemoryBlock> block = findBlock(address, true);
		if (block != null) {
			return allocatedBlocks.remove(block.getKey(), block.getValue());
		}
		return false;
	}

	@Override
	public boolean freeDirect(long address) {
		Map.Entry<MemoryAddress, MemoryBlock> block = findBlock(address, false);
		if (block != null) {
			return allocatedBlocks.remove(block.getKey(), block.getValue());
		}
		return false;
	}

	@Override
	public MemoryAllocatorStatistics dumpStatistics() {
		return null;
	}

	@Override
	public MemoryAllocatorStatistics liveStatistics() {
		return null;
	}

	@Override
	protected boolean canAllocate(long bytes) {
		return bytes < Integer.MAX_VALUE - 12;
	}

	@Override
	protected MemoryBlock makeBlock(long address, long bytes, boolean heap) {
		return new SimpleMemoryBlock(address, MemoryData.buffer(ByteBuffer.allocate((int) bytes).order(ORDER)), heap);
	}

	private Map.Entry<MemoryAddress, MemoryBlock> findBlock(long address, boolean heap) {
		MemoryAddress wrapper = ThreadLocalStorage.get().memoryAddress(address);
		Map.Entry<MemoryAddress, MemoryBlock> entry = allocatedBlocks.floorEntry(wrapper);
		if (entry != null) {
			MemoryBlock block = entry.getValue();
			if (heap == block.isHeap() && address - block.getAddress() < block.getData().length()) {
				return entry;
			}
		}
		return null;
	}

	private MemoryBlock makeNewBlock(long bytes, boolean onHeap) {
		if (!canAllocate(bytes)) {
			return null;
		}
		NavigableMap<MemoryAddress, MemoryBlock> allocatedBlocks = this.allocatedBlocks;
		// Use random strategy to find free address
		ThreadLocalRandom rng = ThreadLocalRandom.current();
		MemoryAddress address = ThreadLocalStorage.get().memoryAddress();
		long rawAddress;
		while (true) {
			rawAddress = rng.nextLong();
			if (rawAddress == 0L) {
				continue;
			}
			address.set(rawAddress);
			if (allocatedBlocks.isEmpty()) {
				break;
			}
			Map.Entry<MemoryAddress, MemoryBlock> presentAddress = allocatedBlocks.floorEntry(address);
			if (presentAddress == null) {
				presentAddress = allocatedBlocks.firstEntry();
			}
			MemoryBlock block = presentAddress.getValue();
			long existingAddress = block.getAddress();
			long blockLength = block.getData().length();
			if (rawAddress >= existingAddress && rawAddress <= existingAddress + blockLength) {
				continue;
			}
			break;
		}
		MemoryBlock block = makeBlock(rawAddress, bytes, onHeap);
		allocatedBlocks.put(address.copy(), block);
		return block;
	}
}
