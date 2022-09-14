package dev.xdark.ssvm.synchronizer.java;

import dev.xdark.ssvm.synchronizer.Mutex;
import dev.xdark.ssvm.synchronizer.ObjectSynchronizer;

import java.util.BitSet;

/**
 * Synchronizer that uses Java locks.
 *
 * @author xDark
 */
public final class LockObjectSynchronizer implements ObjectSynchronizer {
	private final BitSet free;
	private final Mutex[] mutexes;

	public LockObjectSynchronizer(int maxLocks) {
		free = new BitSet(maxLocks);
		mutexes = new Mutex[maxLocks];
	}

	public LockObjectSynchronizer() {
		this(65535);
	}

	@Override
	public Mutex acquire() {
		int slot;
		synchronized (this) {
			BitSet free = this.free;
			slot = free.nextClearBit(0);
			free.set(slot);
		}
		Mutex[] mutexes = this.mutexes;
		Mutex mutex = mutexes[slot];
		if (mutex == null) {
			mutexes[slot] = mutex = new LockMutex(slot);
		}
		return mutex;
	}

	@Override
	public Mutex get(int id) {
		return mutexes[id];
	}

	@Override
	public void free(Mutex mutex) {
		free.clear(mutex.id());
	}
}
