package dev.xdark.ssvm.value;

import dev.xdark.ssvm.memory.management.MemoryManager;
import dev.xdark.ssvm.memory.allocation.MemoryBlock;
import dev.xdark.ssvm.mirror.JavaClass;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Represents VM object value.
 *
 * @author xDark
 */
public abstract class SimpleObjectValue implements ObjectValue {

	private final ReentrantLock lock;
	private final Condition signal;
	private final MemoryManager memoryManager;
	protected final MemoryBlock memory;

	/**
	 * @param memoryManager Memory manager.
	 * @param memory        Object data.
	 */
	public SimpleObjectValue(MemoryManager memoryManager, MemoryBlock memory) {
		this.memoryManager = memoryManager;
		this.memory = memory;
		ReentrantLock lock = new ReentrantLock();
		this.lock = lock;
		signal = lock.newCondition();
	}

	@Override
	public long asLong() {
		return memory.getData().readLong(0L);
	}

	@Override
	public int asInt() {
		return memory.getData().readInt(0L);
	}

	@Override
	public char asChar() {
		return memory.getData().readChar(0L);
	}

	@Override
	public short asShort() {
		return memory.getData().readShort(0L);
	}

	@Override
	public byte asByte() {
		return memory.getData().readByte(0L);
	}

	@Override
	public boolean asBoolean() {
		return asByte() != 0;
	}

	@Override
	public JavaClass getJavaClass() {
		return getMemoryManager().readClass(this);
	}

	@Override
	public MemoryBlock getMemory() {
		return memory;
	}

	@Override
	public void monitorEnter() {
		lock.lock();
	}

	@Override
	public boolean monitorExit() {
		ReentrantLock lock = this.lock;
		if (!lock.isHeldByCurrentThread()) {
			return false;
		}
		lock.unlock();
		return true;
	}

	@Override
	public void monitorWait(long timeoutMillis) throws InterruptedException {
		signal.await(timeoutMillis, TimeUnit.MILLISECONDS);
	}

	@Override
	public void monitorNotify() {
		signal.signal();
	}

	@Override
	public void monitorNotifyAll() {
		signal.signalAll();
	}

	@Override
	public boolean isHeldByCurrentThread() {
		return lock.isHeldByCurrentThread();
	}

	protected MemoryManager getMemoryManager() {
		return memoryManager;
	}
}
