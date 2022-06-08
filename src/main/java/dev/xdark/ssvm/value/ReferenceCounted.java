package dev.xdark.ssvm.value;

/**
 * Reference counted GC value.
 *
 * @author xDark
 */
public interface ReferenceCounted {

	/**
	 * @return current reference count.
	 */
	long refCount();

	/**
	 * Increases reference count.
	 *
	 * @param count
	 * 		Count to increase reference count by.
	 *
	 * @return this value.
	 */
	ReferenceCounted retain(long count);

	/**
	 * Increases reference count.
	 *
	 * @return this value.
	 */
	ReferenceCounted retain();

	/**
	 * Decreases reference count.
	 *
	 * @param count
	 * 		Count to decrease reference count by.
	 *
	 * @return this value.
	 */
	boolean release(long count);

	/**
	 * Increases reference count.
	 *
	 * @return this value.
	 */
	boolean release();
}
