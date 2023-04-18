package dev.xdark.ssvm.memory.gc;

import java.util.concurrent.atomic.AtomicLongFieldUpdater;

/**
 * Simple GC handle.
 *
 * @author xDark
 */
public abstract class SimpleGCHandle implements GCHandle {
	private static final AtomicLongFieldUpdater<SimpleGCHandle> UPDATER = AtomicLongFieldUpdater.newUpdater(SimpleGCHandle.class, "count");
	private volatile long count = 1L;

	@Override
	public GCHandle retain() {
		long count;
		do {
			count = this.count;
		} while (!UPDATER.compareAndSet(this, count, count + 1L));
		return this;
	}

	@Override
	public boolean release() {
		long count, newCount;
		do {
			count = this.count;
			if (count == 0L) {
				throw new IllegalStateException();
			}
			newCount = count - 1L;
		} while (!UPDATER.compareAndSet(this, count, newCount));
		if (newCount == 0L) {
			deallocate();
			return true;
		}
		return false;
	}

	protected abstract void deallocate();
}
