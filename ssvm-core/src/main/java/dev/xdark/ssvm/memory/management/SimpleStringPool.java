package dev.xdark.ssvm.memory.management;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.operation.VMOperations;
import dev.xdark.ssvm.value.InstanceValue;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;

/**
 * Basic string pool implementation.
 *
 * @author xDark
 */
public class SimpleStringPool implements StringPool {

	private final Map<String, InstanceValue> pool = new HashMap<>();
	private final ReadWriteLock lock = new ReentrantReadWriteLock();
	private final VirtualMachine vm;
	private final Function<? super String, ? extends InstanceValue> pooler;

	public SimpleStringPool(VirtualMachine vm) {
		this.vm = vm;
		pooler = s -> vm.getOperations().newUtf8(s);
	}

	@Override
	public InstanceValue intern(String value) {
		Lock lock = this.lock.writeLock();
		lock.lock();
		try {
			return pool.computeIfAbsent(value, pooler);
		} finally {
			lock.unlock();
		}
	}

	@Override
	public InstanceValue intern(InstanceValue value) {
		return (InstanceValue) intern(vm.getOperations().readUtf8(value));
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
