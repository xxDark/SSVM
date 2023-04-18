package dev.xdark.ssvm.memory.gc;

/**
 * GC object handle.
 * Object will not be deallocated until
 * all handles are released.
 *
 * @author xDark
 */
public interface GCHandle extends AutoCloseable {

	/**
	 * Retains the handle.
	 *
	 * @return this handle.
	 */
	GCHandle retain();

	/**
	 * Releases GC handle.
	 *
	 * @return {@code true} if that is the
	 * last hanle for an object.
	 */
	boolean release();

	@Override
	default void close() {
		release();
	}
}
