package dev.xdark.ssvm.memory.management;

import dev.xdark.ssvm.util.Helper;
import dev.xdark.ssvm.value.InstanceValue;
import dev.xdark.ssvm.value.ObjectValue;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Basic string pool implementation.
 *
 * @author xDark
 */
public class SimpleStringPool implements StringPool {

	private final Map<String, InstanceValue> pool = new HashMap<>();
	private final ReadWriteLock lock = new ReentrantReadWriteLock();
	private final Helper helper;

	public SimpleStringPool(Helper helper) {
		this.helper = helper;
	}

	@Override
	public ObjectValue intern(String value) {
		Lock lock = this.lock.writeLock();
		lock.lock();
		try {
			return pool.computeIfAbsent(value, k -> {
				return helper.newUtf8(k, false);
			});
		} finally {
			lock.unlock();
		}
	}

	@Override
	public InstanceValue intern(InstanceValue value) {
		return (InstanceValue) intern(helper.readUtf8(value));
	}

	@Override
	public InstanceValue getIfPresent(String str) {
		Lock lock = this.lock.readLock();
		lock.lock();
		try {
			return pool.get(str);
		} finally {
			lock.unlock();
		}
	}
}
