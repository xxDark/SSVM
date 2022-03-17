package dev.xdark.ssvm.thread;

import dev.xdark.ssvm.util.ArraySlice;
import dev.xdark.ssvm.value.Value;

import java.util.List;

/**
 * @see ArraySlice
 */
public final class ThreadRegion extends ArraySlice<Value> implements AutoCloseable {

	private final ThreadStorage storage;

	ThreadRegion(List<Value> array, ThreadStorage storage) {
		super(array);
		this.storage = storage;
	}

	@Override
	public ThreadRegion slice(int fromIndex, int toIndex) {
		return new ThreadRegion(array.subList(fromIndex, toIndex), storage);
	}

	@Override
	public void close() {
		storage.pop(length());
	}
}