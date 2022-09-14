package dev.xdark.ssvm.thread;

import dev.xdark.ssvm.jvmti.ThreadState;

/**
 * Basic OS thread.
 *
 * @author xDark
 */
public abstract class AbstractOSThread implements OSThread {

	private String name;
	private int priority = Thread.NORM_PRIORITY;
	private ThreadState state = ThreadState.JVMTI_THREAD_STATE_TERMINATED;

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public void setPriority(int priority) {
		this.priority = priority;
	}

	@Override
	public void setState(ThreadState state) {
		this.state = state;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public int getPriority() {
		return priority;
	}

	@Override
	public ThreadState getState() {
		return state;
	}
}
