package dev.xdark.ssvm.threadlocal;

import dev.xdark.ssvm.filesystem.Handle;
import dev.xdark.ssvm.memory.allocation.MemoryAddress;
import me.coley.cafedude.io.ClassFileReader;

/**
 * SSVM TLC storage.
 *
 * @author xDark
 */
public final class ThreadLocalStorage {
	private static final ThreadLocal<ThreadLocalStorage> TLC = ThreadLocal.withInitial(ThreadLocalStorage::new);
	private final Handle handle = Handle.of(0L);
	private final MemoryAddress address = MemoryAddress.of(0L);
	private final ClassFileReader classFileReader = new ClassFileReader();

	/**
	 * @param handle Handle.
	 * @return wrapper for a handle.
	 */
	public Handle handle(long handle) {
		Handle wrapper = this.handle;
		wrapper.set(handle);
		return wrapper;
	}

	/**
	 * @return wrapper for a handle.
	 */
	public Handle handle() {
		return handle;
	}

	/**
	 * @param address Memory address.
	 * @return wrapper for a memory address.
	 */
	public MemoryAddress memoryAddress(long address) {
		MemoryAddress wrapper = this.address;
		wrapper.set(address);
		return wrapper;
	}

	/**
	 * @return wrapper for a memory address.
	 */
	public MemoryAddress memoryAddress() {
		return address;
	}

	/**
	 * @return class file reader.
	 */
	public ClassFileReader getClassFileReader() {
		return classFileReader;
	}

	/**
	 * @return thread-local data storage.
	 */
	public static ThreadLocalStorage get() {
		return TLC.get();
	}
}
