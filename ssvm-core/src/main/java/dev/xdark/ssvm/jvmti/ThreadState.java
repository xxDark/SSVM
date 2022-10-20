package dev.xdark.ssvm.jvmti;

/**
 * JVMTI thread state.
 * <p>
 * Refer to jvmti.xml
 *
 * @author xDark
 */
public final class ThreadState {

	public static final ThreadState JVMTI_THREAD_STATE_ALIVE = of(0x0001);
	public static final ThreadState JVMTI_THREAD_STATE_TERMINATED = of(0x0002);
	public static final ThreadState JVMTI_THREAD_STATE_RUNNABLE = of(0x0004);
	public static final ThreadState JVMTI_THREAD_STATE_WAITING = of(0x0080);
	public static final ThreadState JVMTI_THREAD_STATE_WAITING_INDEFINITELY = of(0x0010);
	public static final ThreadState JVMTI_THREAD_STATE_WAITING_WITH_TIMEOUT = of(0x0020);
	public static final ThreadState JVMTI_THREAD_STATE_SLEEPING = of(0x0040);
	public static final ThreadState JVMTI_THREAD_STATE_IN_OBJECT_WAIT = of(0x0100);
	public static final ThreadState JVMTI_THREAD_STATE_PARKED = of(0x0200);
	public static final ThreadState JVMTI_THREAD_STATE_BLOCKED_ON_MONITOR_ENTER = of(0x0400);
	public static final ThreadState JVMTI_THREAD_STATE_SUSPENDED = of(0x100000);
	public static final ThreadState JVMTI_THREAD_STATE_INTERRUPTED = of(0x200000);
	public static final ThreadState JVMTI_THREAD_STATE_IN_NATIVE = of(0x400000);

	// Common ThreadState combinations.
	public static final ThreadState JVMTI_JAVA_LANG_THREAD_STATE_RUNNABLE = JVMTI_THREAD_STATE_ALIVE.with(JVMTI_THREAD_STATE_RUNNABLE);
	public static final ThreadState JVMTI_JAVA_LANG_THREAD_STATE_BLOCKED = JVMTI_THREAD_STATE_ALIVE.with(JVMTI_THREAD_STATE_BLOCKED_ON_MONITOR_ENTER);
	public static final ThreadState JVMTI_JAVA_LANG_THREAD_STATE_WAITING = JVMTI_THREAD_STATE_ALIVE.with(JVMTI_THREAD_STATE_WAITING).with(JVMTI_THREAD_STATE_WAITING_INDEFINITELY);
	public static final ThreadState JVMTI_JAVA_LANG_THREAD_STATE_TIMED_WAITING = JVMTI_THREAD_STATE_ALIVE.with(JVMTI_THREAD_STATE_WAITING).with(JVMTI_THREAD_STATE_WAITING_WITH_TIMEOUT);

	private final int mask;

	private ThreadState(int mask) {
		this.mask = mask;
	}

	public ThreadState with(ThreadState other) {
		return new ThreadState(mask | other.mask);
	}

	public int mask() {
		return mask;
	}

	public boolean has(int flag) {
		return (mask & flag) == flag;
	}

	public boolean has(ThreadState state) {
		return has(state.mask);
	}

	private static ThreadState of(int mask) {
		return new ThreadState(mask);
	}
}
