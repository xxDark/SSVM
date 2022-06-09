package dev.xdark.ssvm.execution;

import java.util.concurrent.atomic.AtomicLongFieldUpdater;

/**
 * Safepoint implementation that uses atomic counter.
 *
 * @author xDark
 */
public class AtomicSafePoint implements SafePoint {
	private static final AtomicLongFieldUpdater<AtomicSafePoint> UPDATER = AtomicLongFieldUpdater.newUpdater(AtomicSafePoint.class, "count");
	private volatile long count;

	@Override
	public void increment() {
		long count;
		do {
			count = this.count;
		} while (!UPDATER.compareAndSet(this, count, count + 1L));
	}

	@Override
	public void decrement() {
		long count;
		do {
			count = this.count;
			if (count < 0L) {
				throw new IllegalStateException();
			}
		} while (!UPDATER.compareAndSet(this, count, count - 1L));
	}

	@Override
	public boolean tryIncrement() {
		return UPDATER.compareAndSet(this, 0L, 1L);
	}
}
