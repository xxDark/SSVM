package dev.xdark.ssvm.thread;

/**
 * VM thread manager.
 *
 * @author xDark
 */
public interface ThreadManager {

	/**
	 * Returns VM thread from Java thread.
	 *
	 * @param thread
	 * 		Thread to get VM thread from.
	 *
	 * @return VM thread.
	 */
	VMThread getVmThread(Thread thread);

	/**
	 * Assigns VM thread.
	 *
	 * @param thread
	 * 		Thread to assign.
	 */
	void setVmThread(VMThread thread);
}
