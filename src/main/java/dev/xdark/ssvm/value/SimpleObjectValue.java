package dev.xdark.ssvm.value;

import dev.xdark.ssvm.memory.Memory;
import dev.xdark.ssvm.memory.MemoryManager;
import dev.xdark.ssvm.mirror.JavaClass;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Represents VM object value.
 *
 * @author xDark
 */
public class SimpleObjectValue implements ObjectValue {

	private final ReentrantLock lock;
	private final Condition signal;
	private final Memory memory;

	/**
	 * @param memory
	 * 		Object data.
	 */
	public SimpleObjectValue(Memory memory) {
		this.memory = memory;
		ReentrantLock lock = new ReentrantLock();
		this.lock = lock;
		signal = lock.newCondition();
	}

	@Override
	public long asLong() {
		return memory.getAddress();
	}

	@Override
	public boolean isWide() {
		return false;
	}

	@Override
	public boolean isUninitialized() {
		return false;
	}

	@Override
	public boolean isNull() {
		return false;
	}

	@Override
	public boolean isVoid() {
		return false;
	}

	@Override
	public JavaClass getJavaClass() {
		return getMemoryManager().readClass(this);
	}

	@Override
	public Memory getMemory() {
		return memory;
	}

	@Override
	public void monitorEnter() {
		lock.lock();
	}

	@Override
	public void monitorExit() {
		lock.unlock();
	}

	@Override
	public void vmWait(long timeoutMillis) throws InterruptedException {
		signal.await(timeoutMillis, TimeUnit.MILLISECONDS);
	}

	@Override
	public void vmNotify() {
		signal.signal();
	}

	@Override
	public void vmNotifyAll() {
		signal.signalAll();
	}

	@Override
	public boolean isHeldByCurrentThread() {
		return lock.isHeldByCurrentThread();
	}

	protected MemoryManager getMemoryManager() {
		return memory.getMemoryManager();
	}
}
