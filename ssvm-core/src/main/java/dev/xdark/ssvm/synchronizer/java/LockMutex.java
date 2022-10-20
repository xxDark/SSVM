package dev.xdark.ssvm.synchronizer.java;

import dev.xdark.ssvm.synchronizer.Mutex;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

final class LockMutex implements Mutex {

	private final int id;
	private final ReentrantLock lock;
	private final Condition condition;

	LockMutex(int id) {
		this.id = id;
		condition = (lock = new ReentrantLock()).newCondition();
	}

	@Override
	public int id() {
		return id;
	}

	@Override
	public void lock() {
		lock.lock();
	}

	@Override
	public boolean tryUnlock() {
		ReentrantLock lock = this.lock;
		if (!lock.isHeldByCurrentThread()) {
			return false;
		}
		lock.unlock();
		return true;
	}

	@Override
	public void doWait(long timeoutMillis) throws InterruptedException {
		condition.await(timeoutMillis, TimeUnit.MILLISECONDS);
	}

	@Override
	public void doNotify() {
		condition.signal();
	}

	@Override
	public void doNotifyAll() {
		condition.signalAll();
	}

	@Override
	public boolean isHeldByCurrentThread() {
		return lock.isHeldByCurrentThread();
	}
}
