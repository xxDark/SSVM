package dev.xdark.ssvm.thread;

import dev.xdark.ssvm.execution.Locals;
import dev.xdark.ssvm.execution.MemoryLocals;
import dev.xdark.ssvm.execution.MemoryStack;
import dev.xdark.ssvm.execution.Stack;
import dev.xdark.ssvm.memory.allocation.MemoryAllocator;
import dev.xdark.ssvm.memory.allocation.MemoryBlock;
import dev.xdark.ssvm.memory.allocation.MemoryData;
import dev.xdark.ssvm.memory.management.MemoryManager;

public final class AllocatedThreadStorage implements ThreadStorage {
	private static final int VALUE_SCALE = 8;
	private final MemoryManager manager;
	private final MemoryAllocator allocator;
	private final MemoryBlock block;
	private final MemoryData memory;
	private long pointer;

	public AllocatedThreadStorage(MemoryManager manager, MemoryAllocator allocator, MemoryBlock block) {
		this.manager = manager;
		this.allocator = allocator;
		this.block = block;
		memory = block.getData();
	}

	@Override
	public ThreadMemoryData allocate(long size) {
		// TODO stack overflow check
		long pointer = this.pointer;
		this.pointer = pointer + size;
		MemoryData chunk = memory.slice(pointer, size);
		return new ThreadMemoryData() {
			@Override
			public MemoryData data() {
				return chunk;
			}

			@Override
			public void reclaim() {
				AllocatedThreadStorage.this.pointer -= size;
			}
		};
	}

	@Override
	public Stack newStack(int size) {
		return new MemoryStack(manager, allocate((long) size * VALUE_SCALE));
	}

	@Override
	public Locals newLocals(int size) {
		return new MemoryLocals(manager, allocate((long) size * VALUE_SCALE));
	}

	@Override
	public void free() {
		allocator.freeDirect(block.getAddress());
	}
}
