package dev.xdark.ssvm.io;

import dev.xdark.ssvm.threadlocal.ThreadLocalStorage;

/**
 * Handle key for file management.
 *
 * @author xDark
 */
public final class Handle {

	private long value;

	private Handle(long value) {
		this.value = value;
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
	 * @param value New value.
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

	@Override
	public String toString() {
		return String.valueOf(value);
	}

	/**
	 * Returns thread-local handle.
	 *
	 * @param value Raw handle.
	 * @return thread-local handle.
	 */
	public static Handle threadLocal(long value) {
		return ThreadLocalStorage.get().handle(value);
	}

	/**
	 * Returns thread-local handle.
	 *
	 * @return thread-local handle.
	 */
	public static Handle threadLocal() {
		return ThreadLocalStorage.get().handle();
	}

	/**
	 * @param value Handle value.
	 * @return new handle.
	 */
	public static Handle of(long value) {
		return new Handle(value);
	}
}
