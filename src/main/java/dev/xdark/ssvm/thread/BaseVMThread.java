package dev.xdark.ssvm.thread;

import dev.xdark.ssvm.value.InstanceValue;
import lombok.Getter;

/**
 * Basic VMThread implementation.
 *
 * @author xDark
 */
public abstract class BaseVMThread implements VMThread {

	private final Backtrace backtrace = createBacktrace();
	@Getter
	private final ThreadStorage threadStorage = createThreadStorage();

	private InstanceValue oop;

	/**
	 * @param oop VM thread oop.
	 */
	protected BaseVMThread(InstanceValue oop) {
		this.oop = oop;
	}

	protected BaseVMThread() {
	}

	@Override
	public Backtrace getBacktrace() {
		return backtrace;
	}

	@Override
	public InstanceValue getOop() {
		return oop;
	}

	/**
	 * Creates backtrace.
	 *
	 * @return backtrace.
	 */
	protected Backtrace createBacktrace() {
		return new SimpleBacktrace();
	}

	/**
	 * Creates thread storage.
	 *
	 * @return thread storage.
	 */
	protected ThreadStorage createThreadStorage() {
		return SimpleThreadStorage.create();
	}

	/**
	 * @param oop
	 *      Oop to set.
	 */
	public void setOop(InstanceValue oop) {
		if (this.oop != null) {
			throw new IllegalStateException("Cannot change thread instance");
		}
		this.oop = oop;
	}
}
