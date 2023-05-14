package dev.xdark.ssvm.util;

/**
 * Unchecked functional interfaces and utilities.
 */
public class Unchecked {
	public static <T> T supplier(UncheckedSupplier<T> supplier) {
		try {
			return supplier.get();
		} catch (Throwable e) {
			throw new IllegalStateException(e);
		}
	}

	public static <K, V> V function(K key, UncheckedFunction<K, V> function) {
		try {
			return function.apply(key);
		} catch (Throwable e) {
			throw new IllegalStateException(e);
		}
	}

	public interface UncheckedSupplier<T> {
		T get() throws Throwable;
	}

	public interface UncheckedFunction<K, V> {
		V apply(K key) throws Throwable;
	}
}
