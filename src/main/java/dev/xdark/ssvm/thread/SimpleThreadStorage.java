package dev.xdark.ssvm.thread;

import dev.xdark.ssvm.execution.Locals;
import dev.xdark.ssvm.execution.Stack;
import dev.xdark.ssvm.execution.ThreadLocals;
import dev.xdark.ssvm.execution.ThreadStack;
import dev.xdark.ssvm.value.Value;

/**
 * Thread cache for VM.
 *
 * @author xDark
 */
public class SimpleThreadStorage implements ThreadStorage {

	private static final ThreadLocal<SimpleThreadStorage> THREAD_LOCAL = ThreadLocal.withInitial(SimpleThreadStorage::create);
	private static final int DEFAULT_STORAGE_SIZE = 65536;
	private final Value[] storage;
	private int currentIndex;

	private SimpleThreadStorage(int maxSize) {
		storage = new Value[maxSize];
	}

	@Override
	public ThreadRegion push(int size) {
		int currentIndex = this.currentIndex;
		int toIndex = currentIndex + size;
		Value[] storage = this.storage;
		if (toIndex > storage.length) {
			throw new IndexOutOfBoundsException();
		}

		ThreadRegion region = new ThreadRegion(storage, currentIndex, toIndex, this);
		this.currentIndex = toIndex;
		return region;
	}

	@Override
	public void pop(int size) {
		if ((currentIndex -= size) < 0) {
			throw new IndexOutOfBoundsException();
		}
	}

	@Override
	public Stack newStack(int size) {
		return new ThreadStack(push(size));
	}

	@Override
	public Locals newLocals(int size) {
		return new ThreadLocals(push(size));
	}

	/**
	 * Creates new thread storage.
	 *
	 * @param maxSize Storage size.
	 * @return new storage.
	 */
	public static SimpleThreadStorage create(int maxSize) {
		return new SimpleThreadStorage(maxSize);
	}

	/**
	 * Creates new thread storage
	 * of default size.
	 *
	 * @return new storage.
	 */
	public static SimpleThreadStorage create() {
		return new SimpleThreadStorage(DEFAULT_STORAGE_SIZE);
	}

	/**
	 * @return thread-local storage.
	 */
	public static SimpleThreadStorage get() {
		return THREAD_LOCAL.get();
	}

	/**
	 * @see SimpleThreadStorage#get()
	 * @see SimpleThreadStorage#push(int)
	 */
	public static ThreadRegion threadPush(int size) {
		return THREAD_LOCAL.get().push(size);
	}
}
