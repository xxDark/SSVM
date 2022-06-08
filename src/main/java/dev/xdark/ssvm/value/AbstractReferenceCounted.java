package dev.xdark.ssvm.value;

import java.util.concurrent.atomic.AtomicLongFieldUpdater;

public abstract class AbstractReferenceCounted implements ReferenceCounted {
	private static final AtomicLongFieldUpdater<AbstractReferenceCounted> UPDATER
			= AtomicLongFieldUpdater.newUpdater(AbstractReferenceCounted.class, "refCount");
	private volatile long refCount = 1L;

	@Override
	public long refCount() {
		return refCount;
	}

	@Override
	public ReferenceCounted retain(long count) {
		long refCount;
		do {
			refCount = this.refCount;
		} while (!UPDATER.compareAndSet(this, refCount, refCount + count));
		return this;
	}

	@Override
	public ReferenceCounted retain() {
		long refCount;
		do {
			refCount = this.refCount;
		} while (!UPDATER.compareAndSet(this, refCount, refCount + 1L));
		return this;
	}

	@Override
	public boolean release(long count) {
		if (count <= 0L) {
			throw new IllegalArgumentException();
		}
		long refCount, newRefCount;
		do {
			refCount = this.refCount;
			newRefCount = refCount - count;
			if (newRefCount < 0L) {
				throw new IllegalStateException();
			}
		} while (!UPDATER.compareAndSet(this, refCount, newRefCount));
		if (newRefCount == 0L) {
			deallocate();
			return true;
		}
		return false;
	}

	@Override
	public boolean release() {
		long refCount, newRefCount;
		do {
			refCount = this.refCount;
			newRefCount = refCount - 1L;
			if (newRefCount < 0L) {
				throw new IllegalStateException();
			}
		} while (!UPDATER.compareAndSet(this, refCount, newRefCount));
		if (newRefCount == 0L) {
			deallocate();
			return true;
		}
		return false;
	}

	protected abstract void deallocate();
}
