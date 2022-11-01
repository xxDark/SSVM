package dev.xdark.ssvm.synchronizer.java;

import dev.xdark.ssvm.metadata.MetadataStorage;
import dev.xdark.ssvm.metadata.SimpleMetadataStorage;
import dev.xdark.ssvm.synchronizer.Mutex;
import dev.xdark.ssvm.synchronizer.ObjectSynchronizer;

/**
 * Synchronizer that uses Java locks.
 *
 * @author xDark
 * @deprecated Switch to object header for locking
 */
@Deprecated
public final class LockObjectSynchronizer implements ObjectSynchronizer {
	private final MetadataStorage<Mutex> mutexStorage = new SimpleMetadataStorage<>();

	@Override
	public Mutex acquire() {
		LockMutex mutex = new LockMutex();
		mutex.id = mutexStorage.register(mutex);
		return mutex;
	}

	@Override
	public Mutex get(int id) {
		return mutexStorage.lookup(id);
	}

	@Override
	public void free(Mutex mutex) {
	}
}
