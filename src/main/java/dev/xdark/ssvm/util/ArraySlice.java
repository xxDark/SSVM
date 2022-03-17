package dev.xdark.ssvm.util;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.lang.reflect.Array;
import java.util.List;

/**
 * Slice of the array
 *
 * @param <V>
 *      Component type.
 *
 * @author xDark
 */
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ArraySlice<V> {

	protected List<V> array;

	/**
	 * Sets element.
	 *
	 * @param index
	 *      Element index.
	 * @param value
	 *      Eleemnt value.
	 */
	public void set(int index, V value) {
		array.set(index, value);
	}

	/**
	 * @param index
	 *      Element index.
	 *
	 * @return element by it's index.
	 */
	public V get(int index) {
		return array.get(index);
	}

	/**
	 * @return size of array slice.
	 */
	public int length() {
		return array.size();
	}

	/**
	 * @param typeHint
	 *      Type hint to create an array.
	 *
	 * @return backing array.
	 */
	public V[] unwrap(V... typeHint) {
		return array.toArray((V[]) Array.newInstance(typeHint.getClass().getComponentType(), 0));
	}

	/**
	 * @param fromIndex
	 *      Starting index.
	 * @param toIndex
	 *      Ending index.
	 *
	 * @return slice of this slice.
	 */
	public ArraySlice<V> slice(int fromIndex, int toIndex) {
		return new ArraySlice<>(array.subList(fromIndex, toIndex));
	}
}
