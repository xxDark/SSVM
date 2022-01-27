package dev.xdark.ssvm.thread;

/**
 * Thread implementation that ignores every call.
 *
 * @author xDark
 */
public final class NopThread extends Thread {

	public NopThread() {
	}

	public NopThread(Runnable target) {
		super(target);
	}

	public NopThread(ThreadGroup group, Runnable target) {
		super(group, target);
	}

	public NopThread(String name) {
		super(name);
	}

	public NopThread(ThreadGroup group, String name) {
		super(group, name);
	}

	public NopThread(Runnable target, String name) {
		super(target, name);
	}

	public NopThread(ThreadGroup group, Runnable target, String name) {
		super(group, target, name);
	}

	public NopThread(ThreadGroup group, Runnable target, String name, long stackSize) {
		super(group, target, name, stackSize);
	}

	@Override
	public synchronized void start() {

	}
}
