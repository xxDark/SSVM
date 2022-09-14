package dev.xdark.ssvm.memory.management;

import dev.xdark.ssvm.VirtualMachine;
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
	private final VirtualMachine vm;

	/**
	 * @param vm VM instance.
	 */
	public SimpleStringPool(VirtualMachine vm) {
		this.vm = vm;
	}

	@Override
	public ObjectValue intern(String value) {
		if (value == null) {
			return vm.getMemoryManager().nullValue();
		}
		Lock lock = this.lock.writeLock();
		lock.lock();
		try {
			return pool.computeIfAbsent(value, k -> {
				return (InstanceValue) vm.getHelper().newUtf8(value, false);
			});
		} finally {
			lock.unlock();
		}
	}

	@Override
	public InstanceValue intern(InstanceValue value) {
		return (InstanceValue) intern(vm.getHelper().readUtf8(value));
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
