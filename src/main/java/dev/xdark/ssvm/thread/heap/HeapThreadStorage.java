package dev.xdark.ssvm.thread.heap;

import dev.xdark.ssvm.execution.EmptyLocals;
import dev.xdark.ssvm.execution.EmptyStack;
import dev.xdark.ssvm.execution.Locals;
import dev.xdark.ssvm.execution.PanicException;
import dev.xdark.ssvm.execution.Stack;
import dev.xdark.ssvm.memory.allocation.MemoryAllocator;
import dev.xdark.ssvm.memory.allocation.MemoryBlock;
import dev.xdark.ssvm.memory.allocation.MemoryData;
import dev.xdark.ssvm.memory.allocation.SliceMemoryData;
import dev.xdark.ssvm.memory.management.MemoryManager;
import dev.xdark.ssvm.thread.ThreadMemoryData;
import dev.xdark.ssvm.thread.ThreadStorage;
import dev.xdark.ssvm.util.BoundedQueue;

import java.util.ArrayDeque;
import java.util.Queue;

public final class HeapThreadStorage implements ThreadStorage {
	private static final int VALUE_SCALE = 8;
	private final Queue<HeapComponent> queue = new BoundedQueue<>(new ArrayDeque<>(), 128);
	private final MemoryManager manager;
	private final MemoryAllocator allocator;
	private final MemoryBlock block;
	private final MemoryData memory;
	private long pointer;

	public HeapThreadStorage(MemoryManager manager, MemoryAllocator allocator, MemoryBlock block) {
		this.manager = manager;
		this.allocator = allocator;
		this.block = block;
		memory = block.getData();
	}

	@Override
	public Stack newStack(int size) {
		if (size == 0) {
			return EmptyStack.INSTANCE;
		}
		HeapComponent hc = pollComponent();
		doAllocate(hc, size *= VALUE_SCALE);
		return hc.makeStack(size);
	}

	@Override
	public Locals newLocals(int size) {
		if (size == 0) {
			return EmptyLocals.INSTANCE;
		}
		HeapComponent hc = pollComponent();
		doAllocate(hc, size *= VALUE_SCALE);
		return hc.makeLocals(size);
	}

	@Override
	public void free() {
		if (!allocator.freeHeap(block.getAddress())) {
			throw new PanicException("Failed to free thread block");
		}
	}

	private void doAllocate(HeapComponent component, long size) {
		long pointer = this.pointer;
		this.pointer = pointer + size;
		SliceMemoryData slice = component.slice;
		if (slice == null) {
			slice = new SliceMemoryData();
			component.slice = slice;
		}
		slice.init(memory, pointer, size);
	}

	private HeapComponent pollComponent() {
		HeapComponent component = queue.poll();
		if (component == null) {
			return new HeapComponent();
		}
		return component;
	}

	private final class HeapComponent {
		SliceMemoryData slice;
		HeapStack stack;
		HeapLocals locals;
		ThreadMemoryDataImpl threadData;

		Stack makeStack(long size) {
			ThreadMemoryData data = makeThreadData(size);
			HeapStack stack = this.stack;
			if (stack == null) {
				stack = new HeapStack(manager, data);
				this.stack = stack;
			} else {
				stack.reset(data);
			}
			return stack;
		}

		Locals makeLocals(long size) {
			ThreadMemoryData data = makeThreadData(size);
			HeapLocals locals = this.locals;
			if (locals == null) {
				locals = new HeapLocals(manager, data);
				this.locals = locals;
			} else {
				locals.reset(data);
			}
			return locals;
		}

		ThreadMemoryData makeThreadData(long size) {
			ThreadMemoryDataImpl threadData = this.threadData;
			if (threadData == null) {
				threadData = new ThreadMemoryDataImpl(this);
				this.threadData = threadData;
			}
			threadData.data = slice;
			threadData.size = size;
			return threadData;
		}
	}

	private final class ThreadMemoryDataImpl implements ThreadMemoryData {
		final HeapComponent attachment;
		MemoryData data;
		long size;

		ThreadMemoryDataImpl(HeapComponent attachment) {
			this.attachment = attachment;
		}

		@Override
		public MemoryData data() {
			return data;
		}

		@Override
		public void reclaim() {
			HeapThreadStorage ts = HeapThreadStorage.this;
			ts.pointer = ts.pointer - size;
			ts.queue.offer(attachment);
		}
	}
}
