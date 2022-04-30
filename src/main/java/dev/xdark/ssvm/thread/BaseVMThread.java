package dev.xdark.ssvm.thread;

import dev.xdark.ssvm.value.InstanceValue;
import lombok.Getter;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Basic VMThread implementation.
 *
 * @author xDark
 */
public abstract class BaseVMThread implements VMThread {

	private final Backtrace backtrace = createBacktrace();
	@Getter
	private final Queue<Runnable> taskQueue = new ConcurrentLinkedQueue<>();
	@Getter
	private final ThreadStorage threadStorage = createThreadStorage();

	private final InstanceValue oop;

	/**
	 * @param oop
	 * 		VM thread oop.
	 */
	protected BaseVMThread(InstanceValue oop) {
		this.oop = oop;
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
}
