package dev.xdark.ssvm.execution;

/**
 * No-op safepoint implementation.
 *
 * @author xDark
 */
public final class NoopSafePoint implements SafePoint {

	@Override
	public void increment() {
	}

	@Override
	public void decrement() {
	}

	@Override
	public boolean tryAcquire() {
		return true;
	}
}
