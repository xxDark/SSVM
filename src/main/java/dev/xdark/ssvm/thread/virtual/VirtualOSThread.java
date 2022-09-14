package dev.xdark.ssvm.thread.virtual;

import dev.xdark.ssvm.thread.ThreadStorage;
import dev.xdark.ssvm.thread.backtrace.Backtrace;
import dev.xdark.ssvm.thread.AbstractOSThread;

/**
 * Virtual OS thread.
 *
 * @author xDark
 */
final class VirtualOSThread extends AbstractOSThread {
	private final Backtrace backtrace;
	private final ThreadStorage storage;

	VirtualOSThread(Backtrace backtrace, ThreadStorage storage) {
		this.backtrace = backtrace;
		this.storage = storage;
	}

	@Override
	public Backtrace getBacktrace() {
		return backtrace;
	}

	@Override
	public ThreadStorage getStorage() {
		return storage;
	}

	void free() {
		storage.free();
	}
}
