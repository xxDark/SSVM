package dev.xdark.ssvm.thread;

import dev.xdark.ssvm.jvmti.ThreadState;
import dev.xdark.ssvm.thread.backtrace.Backtrace;

/**
 * OS thread.
 * <p>
 * All methods in this class <b>must</b> be called
 * only inside of {@link ThreadManager}.
 *
 * @author xDark
 * @see ThreadManager
 */
public interface OSThread {

	/**
	 * Sets thread name.
	 *
	 * @param name New thread name.
	 */
	void setName(String name);

	/**
	 * Sets thread priority.
	 *
	 * @param priority New priority.
	 */
	void setPriority(int priority);

	/**
	 * Sets new thread state.
	 *
	 * @param state New thread state.
	 */
	void setState(ThreadState state);

	/**
	 * @return Thread name.
	 */
	String getName();

	/**
	 * @return Thread priority.
	 */
	int getPriority();

	/**
	 * @return Thread state.
	 */
	ThreadState getState();

	/**
	 * @return Backtrace or {@code null},
	 * if thread is not alive.
	 */
	Backtrace getBacktrace();

	/**
	 * @return Storage or {@code null},
	 * if thread is not alive.
	 */
	ThreadStorage getStorage();
}
