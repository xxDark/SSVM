package dev.xdark.ssvm.thread.nop;

import dev.xdark.ssvm.thread.BaseVMThread;
import dev.xdark.ssvm.thread.ThreadStorage;
import dev.xdark.ssvm.thread.backtrace.Backtrace;
import dev.xdark.ssvm.value.InstanceValue;

/**
 * VM thread that does not start native thread.
 *
 * @author xDark
 */
public final class NopVMThread extends BaseVMThread {

	private final Thread thread;

	public NopVMThread(Backtrace backtrace, ThreadStorage threadStorage, InstanceValue oop, Thread thread) {
		super(backtrace, threadStorage, oop);
		this.thread = thread;
	}

	public NopVMThread(Backtrace backtrace, ThreadStorage threadStorage, Thread thread) {
		super(backtrace, threadStorage);
		this.thread = thread;
	}

	@Override
	public Thread getJavaThread() {
		return thread;
	}

	@Override
	public void setPriority(int priority) {
	}

	@Override
	public void setName(String name) {
	}

	@Override
	public void start() {

	}

	@Override
	public void interrupt() {
	}

	@Override
	public boolean isAlive() {
		return false;
	}

	@Override
	public boolean isInterrupted(boolean clear) {
		return false;
	}

	@Override
	public void suspend() {
	}

	@Override
	public void resume() {
	}

	@Override
	public void sleep(long millis) throws InterruptedException {
	}
}
