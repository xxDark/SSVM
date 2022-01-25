package dev.xdark.ssvm.thread;

import dev.xdark.ssvm.value.InstanceValue;
import lombok.val;

/**
 * Basic implementation of a VM thread.
 *
 * @author xDark
 */
public final class NativeVMThread extends BaseVMThread {

	private final Thread thread;

	public NativeVMThread(Thread thread, InstanceValue oop) {
		super(oop);
		this.thread = thread;
	}

	@Override
	public Thread getJavaThread() {
		return thread;
	}

	@Override
	public void setPriority(int priority) {
		thread.setPriority(priority);
	}

	@Override
	public void setName(String name) {
		thread.setName(name);
	}

	@Override
	public void start() {
		thread.start();
	}

	@Override
	public void interrupt() {
		thread.interrupt();
	}

	@Override
	public boolean isAlive() {
		return thread.isAlive();
	}

	@Override
	public boolean isInterrupted(boolean clear) {
		val thread = this.thread;
		if (clear && Thread.currentThread() == thread) {
			return Thread.interrupted();
		}
		return thread.isInterrupted();
	}
}
