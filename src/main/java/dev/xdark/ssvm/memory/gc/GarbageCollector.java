package dev.xdark.ssvm.memory.gc;

import dev.xdark.ssvm.value.ObjectValue;

/**
 * Garbage collection interface.
 *
 * @author xDark
 */
public interface GarbageCollector {

	/**
	 * @return the amount of bytes the collector
	 * reserves in the object header.
	 */
	int reservedHeaderSize();

	/**
	 * Creates new GC handle for an object.
	 *
	 * @param value Object to create handle for.
	 * @return created handle.
	 * @throws IllegalArgumentException if value is {@code null}.
	 * @see GCHandle
	 * @see ObjectValue#isNull()
	 */
	GCHandle makeHandle(ObjectValue value);

	/**
	 * Returns GC handle if there is any.
	 *
	 * @param value Value to get handle for.
	 * @return GC handle or {@code null} if none.
	 */
	GCHandle getHandle(ObjectValue value);

	/**
	 * Marks an object as global reference, forever.
	 *
	 * @param value
	 *      Object to mark as global reference.
	 */
	void makeGlobalReference(ObjectValue value);

	/**
	 * Invokes garbage collector.
	 *
	 * @return {@code true} if garbage collector
	 * was invoked, {@code false} otherwise.
	 */
	boolean invoke();
}
