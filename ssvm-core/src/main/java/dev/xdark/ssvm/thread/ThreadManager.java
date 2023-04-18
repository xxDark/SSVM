package dev.xdark.ssvm.thread;

import dev.xdark.ssvm.thread.backtrace.Backtrace;
import dev.xdark.ssvm.value.InstanceValue;

import java.util.List;

/**
 * Thread manager.
 *
 * @author xDark
 * @see OSThread
 */
public interface ThreadManager {

	/**
	 * Starts the thread, assigns new OS thread to
	 * the {@code oop} instance.
	 *
	 * @param oop Thread to start.
	 */
	void startThread(InstanceValue oop);

	/**
	 * Suspends the thread.
	 *
	 * @param oop Thread to suspend.
	 */
	void suspendThread(InstanceValue oop);

	/**
	 * Resumes the thread.
	 *
	 * @param oop Thread to resume.
	 */
	void resumeThread(InstanceValue oop);

	/**
	 * Sets thread priority.
	 *
	 * @param oop      Thread oop.
	 * @param priority New priority.
	 */
	void setPriority(InstanceValue oop, int priority);

	/**
	 * Interrupts the thread.
	 *
	 * @param oop Thread to interrupt.
	 */
	void interrupt(InstanceValue oop);

	/**
	 * Sets thread name.
	 *
	 * @param oop  Thread oop.
	 * @param name New name.
	 */
	void setName(InstanceValue oop, String name);

	/**
	 * Stops the thread.
	 *
	 * @param oop       Thread to stop.
	 * @param exception Exception to propagate.
	 */
	void stop(InstanceValue oop, InstanceValue exception);

	/**
	 * @return Current Java thread or {@code null},
	 * if current thread is not VM thread or not attached.
	 */
	JavaThread currentJavaThread();

	/**
	 * @return Current OS thread or {@code null},
	 * if current thread is not VM thread or not attached.
	 * This method may be a bit faster than calling {@code currentJavaThread}
	 * and then {@link JavaThread#getOsThread()}.
	 */
	OSThread currentOsThread();

	/**
	 * @return Current OS thread storage, or {@code null},
	 * if current thread is not VM thread or not attached.
	 */
	default ThreadStorage currentThreadStorage() {
		OSThread th = currentOsThread();
		return th == null ? null : th.getStorage();
	}

	/**
	 * @return Current thread backtrace, or {@code null},
	 * if current thread is not VM thread or not attached.
	 */
	default Backtrace currentBacktrace() {
		OSThread th = currentOsThread();
		return th == null ? null : th.getBacktrace();
	}

	/**
	 * Attaches current thread.
	 */
	void attachCurrentThread();

	/**
	 * Detaches current thread.
	 */
	void detachCurrentThread();

	/**
	 * Checks whether the thread is interrupted.
	 *
	 * @param oop   Thread to check.
	 * @param clear Whether interruption flag should be reset.
	 * @return {@code true} if thread is interrupted.
	 */
	boolean isInterrupted(InstanceValue oop, boolean clear);

	/**
	 * @return A snapshot of currently running Java threads.
	 */
	List<JavaThread> snapshot();

	/**
	 * Causes current thread to sleep.
	 *
	 * @param millis Period to sleep for.
	 */
	void sleep(long millis);

	/**
	 * @see Thread#yield()
	 */
	void yield();

	/**
	 * Creates main thread.
	 * After that call control will be passed into the VM and thread will be
	 * added for scheduling.
	 *
	 * @return Main thread.
	 */
	JavaThread createMainThread();

	/**
	 * @param oop Thread to get Java thread for.
	 * @return Java thread or {@code null}, if thread is not alive.
	 */
	JavaThread getThread(InstanceValue oop);
}
