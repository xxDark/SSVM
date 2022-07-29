package dev.xdark.ssvm.util;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Type-safe map.
 *
 * @author xDark
 */
public final class TypeSafeMap {
	private final Map<Type, Map<?, ?>> typeMap = new HashMap<>();
	private final Function<Type, ? extends Map<?, ?>> mapFunction;

	/**
	 * @param mapSupplier Map supplier.
	 */
	public TypeSafeMap(Supplier<? extends Map<?, ?>> mapSupplier) {
		mapFunction = type -> mapSupplier.get();
	}

	public TypeSafeMap() {
		this(HashMap::new);
	}

	/**
	 * @param type Common key type.
	 * @param key  Key to get value for.
	 * @return value.
	 */
	public <T, K extends T, V> V get(Class<? extends T> type, K key) {
		Map<T, V> map = subMap(type);
		return map.get(key);
	}

	/**
	 * @param key Key to get value for.
	 * @return value.
	 */
	public <V> V get(Object key) {
		Map<Object, V> map = subMap(key.getClass());
		return map.get(key);
	}

	/**
	 * @param type Type of the key of the map.
	 * @param <T>  Key type.
	 * @param <V>  Value type.
	 * @return type-specific map.
	 */
	public <T, V> Map<T, V> subMap(Type type) {
		return (Map<T, V>) typeMap.computeIfAbsent(type, mapFunction);
	}

	/**
	 * @param type Type of the key.
	 * @return type-specific map.
	 */
	public <T, V> Map<T, V> remove(Type type) {
		return (Map<T, V>) typeMap.remove(type);
	}
}
