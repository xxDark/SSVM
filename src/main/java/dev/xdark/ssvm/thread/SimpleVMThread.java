package dev.xdark.ssvm.thread;

import dev.xdark.ssvm.value.InstanceValue;

/**
 * Basic implementation of a VM thread.
 *
 * @author xDark
 */
public final class SimpleVMThread implements VMThread {

	private final Backtrace backtrace = new SimpleBacktrace();
	private final Thread thread;
	private final InstanceValue oop;

	public SimpleVMThread(Thread thread, InstanceValue oop) {
		this.thread = thread;
		this.oop = oop;
	}

	@Override
	public Thread getJavaThread() {
		return thread;
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
