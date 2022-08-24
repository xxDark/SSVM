package dev.xdark.ssvm.thread;

import dev.xdark.ssvm.thread.backtrace.Backtrace;
import dev.xdark.ssvm.thread.backtrace.SimpleBacktrace;
import dev.xdark.ssvm.value.InstanceValue;

/**
 * Basic VMThread implementation.
 *
 * @author xDark
 */
public abstract class BaseVMThread implements VMThread {

	@Deprecated
	private final Backtrace backtrace;
	@Deprecated
	private final ThreadStorage threadStorage;
	private InstanceValue oop;

	/**
	 * @param backtrace     Thread backtrace.
	 * @param threadStorage Thread storage.
	 * @param oop           VM thread oop.
	 */
	protected BaseVMThread(Backtrace backtrace, ThreadStorage threadStorage, InstanceValue oop) {
		this.backtrace = backtrace;
		this.threadStorage = threadStorage;
		this.oop = oop;
	}

	/**
	 * @param backtrace     Thread backtrace.
	 * @param threadStorage Thread storage.
	 */
	protected BaseVMThread(Backtrace backtrace, ThreadStorage threadStorage) {
		this.backtrace = backtrace;
		this.threadStorage = threadStorage;
	}

	@Override
	public Backtrace getBacktrace() {
		return backtrace;
	}

	@Override
	public ThreadStorage getThreadStorage() {
		return threadStorage;
	}

	@Override
	public InstanceValue getOop() {
		return oop;
	}

	/**
	 * @param oop Oop to set.
	 */
	public void setOop(InstanceValue oop) {
		if (this.oop != null) {
			throw new IllegalStateException("Cannot change thread instance");
		}
		this.oop = oop;
	}
}
