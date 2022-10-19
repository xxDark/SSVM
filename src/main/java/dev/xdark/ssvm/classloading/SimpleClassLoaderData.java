package dev.xdark.ssvm.classloading;

import dev.xdark.ssvm.mirror.type.InstanceClass;
import dev.xdark.ssvm.util.CloseableLock;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Simple class storage.
 *
 * @author xDark
 */
public final class SimpleClassLoaderData implements ClassLoaderData {

	private final ReadWriteLock lock = new ReentrantReadWriteLock();
	private final Map<String, InstanceClass> table = new HashMap<>();
	private final Collection<InstanceClass> classesView = Collections.unmodifiableCollection(table.values());
	private final CloseableLock unlocker;

	public SimpleClassLoaderData() {
		// This HAS to be a write lock, because
		// locks cannot be upgraded.
		Lock lock = this.lock.writeLock();
		unlocker = lock::unlock;
	}

	@Override
	public InstanceClass getClass(String name) {
		Lock lock = this.lock.readLock();
		lock.lock();
		try {
			return table.get(name);
		} finally {
			lock.unlock();
		}
	}

	@Override
	public boolean linkClass(InstanceClass jc) {
		Lock lock = this.lock.writeLock();
		lock.lock();
		try {
			String name = jc.getInternalName();
			return table.putIfAbsent(name, jc) == null;
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
	public Collection<InstanceClass> list() {
		return classesView;
	}
}
