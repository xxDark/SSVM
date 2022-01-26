package dev.xdark.ssvm.thread;

import dev.xdark.ssvm.value.InstanceValue;
import lombok.Getter;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Basic VMThread implementation.
 *
 * @author xDark
 */
public abstract class BaseVMThread implements VMThread {

	private final Backtrace backtrace = new SimpleBacktrace();
	@Getter
	private final Queue<Runnable> taskQueue = new ConcurrentLinkedQueue<>();
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
}
