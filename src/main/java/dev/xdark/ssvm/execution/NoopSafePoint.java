package dev.xdark.ssvm.execution;

/**
 * No-op safepoint implementation.
 *
 * @author xDark
 */
public final class NoopSafePoint implements SafePoint {
	@Override
	public boolean poll() {
		return false;
	}

	@Override
	public boolean pollAndSuspend() {
		return false;
	}

	@Override
	public void request() {
	}

	@Override
	public void complete() {
	}
}
