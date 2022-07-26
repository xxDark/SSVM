package dev.xdark.ssvm.util;

import java.util.Arrays;

/**
 * Slice of the array.
 *
 * @param <V> Component type.
 * @author xDark
 */
public class ArraySlice<V> {

	protected final V[] array;
	protected int fromIndex;
	protected int toIndex;

	/**
	 * @param array     Array value.
	 * @param fromIndex From index.
	 * @param toIndex   To index.
	 */
	public ArraySlice(V[] array, int fromIndex, int toIndex) {
		this.array = array;
		this.fromIndex = fromIndex;
		this.toIndex = toIndex;
	}

	/**
	 * Sets element.
	 *
	 * @param index Element index.
	 * @param value Element value.
	 */
	public void set(int index, V value) {
		array[map(index)] = value;
	}

	/**
	 * @param index Element index.
	 * @return element by it's index.
	 */
	public V get(int index) {
		return array[map(index)];
	}

	/**
	 * @return size of array slice.
	 */
	public int length() {
		return toIndex - fromIndex;
	}

	/**
	 * @return backing array.
	 */
	public V[] unwrap() {
		return Arrays.copyOfRange(array, fromIndex, toIndex);
	}

	/**
	 * @param fromIndex Starting index.
	 * @param toIndex   Ending index.
	 * @return slice of this slice.
	 */
	public ArraySlice<V> slice(int fromIndex, int toIndex) {
		return new ArraySlice<>(array, map(fromIndex), toIndex + this.fromIndex);
	}

	/**
	 * @return raw array.
	 */
	public V[] getArray() {
		return array;
	}

	/**
	 * @param index Index to map.
	 * @return new index.
	 */
	public int map(int index) {
		if (index < 0) {
			throw new IndexOutOfBoundsException();
		}
		index += fromIndex;
		if (index >= toIndex) {
			throw new IndexOutOfBoundsException();
		}
		return index;
	}

	/**
	 * Fills the array with some value.
	 *
	 * @param value Value to fill the array with.
	 */
	public void fill(V value) {
		Arrays.fill(array, fromIndex, toIndex, value);
	}

	/**
	 * Relocates slice location.
	 *
	 * @param fromIndex New from index.
	 * @param toIndex   New to index.
	 */
	public void relocate(int fromIndex, int toIndex) {
		this.fromIndex = fromIndex;
		this.toIndex = toIndex;
	}
}
