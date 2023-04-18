package dev.xdark.ssvm.util;

/**
 * Mutable value wrapper.
 *
 * @author xDark
 */
public final class MutableValue<V> {

	private V value;

	public MutableValue(V value) {
		this.value = value;
	}

	public void set(V newValue) {
		value = newValue;
	}

	public V replace(V newValue) {
		V old = value;
		value = newValue;
		return old;
	}

	public V get() {
		return value;
	}
}
