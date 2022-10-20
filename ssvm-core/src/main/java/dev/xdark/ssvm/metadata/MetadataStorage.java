package dev.xdark.ssvm.metadata;

import dev.xdark.ssvm.util.CloseableLock;

import java.util.List;

/**
 * VM metadata storage.
 *
 * @author xDark
 */
public interface MetadataStorage<V> {

	/**
	 * Registers new value.
	 *
	 * @param value Value to register.
	 * @return Value id.
	 */
	int register(V value);

	/**
	 * @param id Value id.
	 * @return Value by it's id or {@code null},
	 * if not found.
	 */
	V lookup(int id);

	/**
	 * @return Acquired lock.
	 */
	CloseableLock lock();

	/**
	 * Returns a list of all values.
	 * The returned collection is unmodifiable and not thread-safe,
	 * this method is suggested to be used in conjunction with
	 * {@link MetadataStorage#lock()}.
	 *
	 * @return All registered values.
	 * @see CloseableLock
	 */
	List<V> list();
}
