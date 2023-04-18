package dev.xdark.ssvm.thread.virtual;

import dev.xdark.ssvm.thread.AbstractOSThread;
import dev.xdark.ssvm.thread.ThreadStorage;
import dev.xdark.ssvm.thread.backtrace.Backtrace;

/**
 * Virtual OS thread.
 *
 * @author xDark
 */
final class VirtualOSThread extends AbstractOSThread {
	private Backtrace backtrace;
	private ThreadStorage storage;

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
		backtrace = null;
		storage = null;
	}
}
