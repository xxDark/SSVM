package dev.xdark.ssvm.thread.java;

import dev.xdark.ssvm.thread.BaseVMThread;
import dev.xdark.ssvm.thread.ThreadHandle;
import dev.xdark.ssvm.thread.ThreadStorage;
import dev.xdark.ssvm.thread.backtrace.Backtrace;
import dev.xdark.ssvm.value.InstanceValue;

public class NativeVMThread extends BaseVMThread {

	private final Thread thread;

	/**
	 * @param backtrace     Thread backtrace.
	 * @param threadStorage Thread storage.
	 * @param oop           VM thread oop.
	 * @param thread        Java thread.
	 */
	public NativeVMThread(Backtrace backtrace, ThreadStorage threadStorage, InstanceValue oop, Thread thread) {
		super(backtrace, threadStorage, oop);
		this.thread = thread;
	}

	/**
	 * @param backtrace     Thread backtrace.
	 * @param threadStorage Thread storage.
	 * @param oop           VM thread oop.
	 * @param group         Thread group.
	 * @param handle        Thread handle.
	 */
	public NativeVMThread(Backtrace backtrace, ThreadStorage threadStorage, InstanceValue oop, ThreadGroup group, ThreadHandle handle) {
		super(backtrace, threadStorage, oop);
		this.thread = new NativeJavaThread(oop, this, group, handle);
	}

	/**
	 * @param backtrace     Thread backtrace.
	 * @param threadStorage Thread storage.
	 * @param oop           VM thread oop.
	 */
	public NativeVMThread(Backtrace backtrace, ThreadStorage threadStorage, InstanceValue oop) {
		super(backtrace, threadStorage, oop);
		this.thread = new NativeJavaThread(oop, this);
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
		Thread thread = this.thread;
		if (clear && Thread.currentThread() == thread) {
			return Thread.interrupted();
		}
		return thread.isInterrupted();
	}

	@Override
	public void suspend() {
		thread.suspend();
	}

	@Override
	public void resume() {
		thread.resume();

	}

	@Override
	public void sleep(long millis) throws InterruptedException {
		if (thread != Thread.currentThread()) {
			throw new IllegalStateException("Called sleep on a wrong thread");
		}
		Thread.sleep(millis);
	}
}
