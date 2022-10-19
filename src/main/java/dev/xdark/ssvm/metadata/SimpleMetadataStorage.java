package dev.xdark.ssvm.metadata;

import dev.xdark.ssvm.util.CloseableLock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Simple metadata storage.
 *
 * @author xDark
 */
public class SimpleMetadataStorage<V> implements MetadataStorage<V> {

	private final List<V> values = new ArrayList<>();
	private final List<V> view = Collections.unmodifiableList(values);
	private final ReadWriteLock lock = new ReentrantReadWriteLock();
	private final CloseableLock unlocker;

	public SimpleMetadataStorage() {
		Lock lock = this.lock.readLock();
		unlocker = lock::unlock;
	}

	@Override
	public int register(V value) {
		Lock lock = this.lock.writeLock();
		lock.lock();
		try {
			List<V> values = this.values;
			int id = values.size();
			values.add(value);
			return afterRegistration(value, id);
		} finally {
			lock.unlock();
		}
	}

	@Override
	public V lookup(int id) {
		if (id < 0) {
			return null;
		}
		Lock lock = this.lock.readLock();
		lock.lock();
		try {
			List<V> values = this.values;
			if (id >= values.size()) {
				return null;
			}
			return values.get(id);
		} finally {
			lock.unlock();
		}
	}

	@Override
	public CloseableLock lock() {
		Lock lock = this.lock.writeLock();
		lock.lock();
		return unlocker;
	}

	@Override
	public List<V> list() {
		return view;
	}

	protected int afterRegistration(V value, int id) {
		return id;
	}
}
