package dev.xdark.ssvm.execution;

/**
 * No-op safepoint implementation.
 *
 * @author xDark
 */
public final class NoopSafePoint implements SafePoint {
	@Override
	public void poll() {
	}

	@Override
	public void request() {
	}
}
