package dev.xdark.ssvm.thread;

import lombok.experimental.UtilityClass;

/**
 * JVMTI thread states.
 *
 * @author xDark
 */
@UtilityClass
public class ThreadState {

	public final int JVMTI_THREAD_STATE_ALIVE = 0x0001;
	public final int JVMTI_THREAD_STATE_TERMINATED = 0x0002;
	public final int JVMTI_THREAD_STATE_RUNNABLE = 0x0004;
	public final int JVMTI_THREAD_STATE_BLOCKED_ON_MONITOR_ENTER = 0x0400;
	public final int JVMTI_THREAD_STATE_WAITING_INDEFINITELY = 0x0010;
	public final int JVMTI_THREAD_STATE_WAITING_WITH_TIMEOUT = 0x0020;
}
