package dev.xdark.ssvm.synchronizer;

/**
 * Object synchronizer.
 *
 * @author xDark
 */
public interface ObjectSynchronizer {

	/**
	 * @return New mutex.
	 */
	Mutex acquire();

	/**
	 * @param id Mutex id.
	 * @return Mutex by it's id.
	 */
	Mutex get(int id);

	// TODO impl when GC is here
	/**
	 * Releases mutex.
	 *
	 * @param mutex Mutex to release.
	 */
	void free(Mutex mutex);
}
