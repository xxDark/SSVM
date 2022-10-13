package dev.xdark.ssvm.mirror.type;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Initialization state.
 *
 * @author xDark
 */
public final class InitializationState {

	private final Lock lock;
	private final Condition condition;
	private volatile InstanceClass.State state;

	InitializationState() {
		Lock lock = new ReentrantLock();
		condition = lock.newCondition();
		this.lock = lock;
		state = InstanceClass.State.PENDING;
	}

	public void lock() {
		lock.lock();
	}

	public void unlock() {
		lock.unlock();
	}

	public Condition condition() {
		return condition;
	}

	public InstanceClass.State get() {
		return state;
	}

	public void set(InstanceClass.State state) {
		this.state = state;
	}

	public boolean is(InstanceClass.State state) {
		return this.state == state;
	}
}
