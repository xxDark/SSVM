package dev.xdark.ssvm.fs;

/**
 * Handle key for file management.
 *
 * @author xDark
 */
public final class Handle {

	private static final ThreadLocal<Handle> HANDLE_TLC = ThreadLocal.withInitial(Handle::new);
	private long value;

	private Handle(long value) {
		this.value = value;
	}

	private Handle() {
	}

	/**
	 * @return handle value.
	 */
	public long get() {
		return value;
	}

	/**
	 * Sets handle value.
	 *
	 * @param value
	 * 		New value.
	 */
	public void set(long value) {
		this.value = value;
	}

	/**
	 * @return copy of this handle.
	 */
	public Handle copy() {
		return new Handle(value);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		return o instanceof Handle && ((Handle) o).value == value;
	}

	@Override
	public int hashCode() {
		return Long.hashCode(value);
	}

	/**
	 * Returns thread-local handle.
	 *
	 * @param value
	 * 		Raw handle.
	 *
	 * @return thread-local handle.
	 */
	public static Handle threadLocal(long value) {
		Handle handle = HANDLE_TLC.get();
		handle.value = value;
		return handle;
	}

	/**
	 * Returns thread-local handle.
	 *
	 * @return thread-local handle.
	 */
	public static Handle threadLocal() {
		return Handle.HANDLE_TLC.get();
	}

	/**
	 * @param value
	 * 		Handle value.
	 *
	 * @return new handle.
	 */
	public static Handle of(long value) {
		return new Handle(value);
	}
}
