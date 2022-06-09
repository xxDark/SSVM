package dev.xdark.ssvm.memory.management;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.memory.gc.GCHandle;
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

	private final Map<String, InternedString> pool = new HashMap<>();
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
				InstanceValue utf8 = (InstanceValue) vm.getHelper().newUtf8(value, false);
				// We never return the string from the pool,
				// so mark it with GC reference and keep it forever
				GCHandle gcHandle = vm.getMemoryManager().getGarbageCollector().makeHandle(utf8);
				return new InternedString(utf8, gcHandle);
			}).reference;
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
			InternedString ref = pool.get(str);
			return ref == null ? null : ref.reference;
		} finally {
			lock.unlock();
		}
	}

	private static final class InternedString {
		final InstanceValue reference;
		final GCHandle gcHandle;

		InternedString(InstanceValue reference, GCHandle gcHandle) {
			this.reference = reference;
			this.gcHandle = gcHandle;
		}
	}
}
