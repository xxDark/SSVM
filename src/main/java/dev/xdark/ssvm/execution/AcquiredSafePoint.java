package dev.xdark.ssvm.execution;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Acquired safepoint for try-with-resources.
 *
 * @author xDark
 */
public final class AcquiredSafePoint implements AutoCloseable {
	private final AtomicBoolean released = new AtomicBoolean();
	private final SafePoint safePoint;

	/**
	 * @param safePoint Safepoint instance.
	 */
	public AcquiredSafePoint(SafePoint safePoint) {
		this.safePoint = safePoint;
	}

	@Override
	public void close() {
		if (!released.compareAndSet(false, true)) {
			throw new IllegalStateException("Already released");
		}
		safePoint.decrement();
	}
}
