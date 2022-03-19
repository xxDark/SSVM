package dev.xdark.ssvm.thread;

import dev.xdark.ssvm.util.ArraySlice;
import dev.xdark.ssvm.value.Value;

/**
 * @see ArraySlice
 */
public final class ThreadRegion extends ArraySlice<Value> implements AutoCloseable {

	private final ThreadStorage storage;

	public ThreadRegion(Value[] array, int fromIndex, int toIndex, ThreadStorage storage) {
		super(array, fromIndex, toIndex);
		this.storage = storage;
	}

	@Override
	public ThreadRegion slice(int fromIndex, int toIndex) {
		return new ThreadRegion(array, map(fromIndex), map(toIndex), storage);
	}

	@Override
	public void close() {
		storage.pop(length());
	}
}