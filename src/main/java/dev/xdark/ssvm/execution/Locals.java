package dev.xdark.ssvm.execution;

import dev.xdark.ssvm.thread.SimpleThreadStorage;
import dev.xdark.ssvm.thread.ThreadRegion;
import dev.xdark.ssvm.value.TopValue;
import dev.xdark.ssvm.value.Value;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.Arrays;
import java.util.Objects;

/**
 * Storage for local variables.
 *
 * @author xDark
 */
@RequiredArgsConstructor
public final class Locals implements AutoCloseable {

	private final ThreadRegion table;

	/**
	 * @param maxSize
	 * 		The maximum amount of local variables.
	 */
	public Locals(int maxSize) {
		table = SimpleThreadStorage.threadPush(maxSize);
	}

	/**
	 * Sets value at specific index.
	 *
	 * @param index
	 * 		Index of local variable.
	 * @param value
	 * 		Value to set.
	 */
	public void set(int index, Value value) {
		table.set(index, Objects.requireNonNull(value, "value"));
	}

	/**
	 * Sets wide value at specific index.
	 *
	 * @param index
	 * 		Index of local variable.
	 * @param value
	 * 		Value to set.
	 */
	public void setWide(int index, Value value) {
		if (!Objects.requireNonNull(value, "value").isWide()) {
			throw new IllegalStateException("Must use set instead");
		}
		val table = this.table;
		table.set(index, value);
		table.set(index + 1, TopValue.INSTANCE);
	}

	/**
	 * Loads value from local variable.
	 *
	 * @param index
	 * 		Index of local variable.
	 * @param <V>
	 * 		Type of the value to load.
	 *
	 * @return value at {@code index}.
	 */
	public <V extends Value> V load(int index) {
		return (V) table.get(index);
	}

	/**
	 * @return underlying content of the LVT.
	 */
	public Value[] getTable() {
		return table.unwrap();
	}

	/**
	 * @return the maximum amount of slots of this LVT.
	 */
	public int maxSlots() {
		return table.length();
	}

	/**
	 * Deallocates internal table.
	 */
	public void deallocate() {
		table.close();
	}

	@Override
	public void close() {
		deallocate();
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) return true;
		if (!(o instanceof Locals)) return false;
		val other = (Locals) o;
		val table = this.table;
		int length = table.length();
		val otherTable = other.table;
		if (table.length() != otherTable.length()) return false;
		for (int i = 0; i < length; i++) {
			if (!Objects.equals(table.get(i), otherTable.get(i))) {
				return false;
			}
		}
		return true;
	}

	@Override
	public int hashCode() {
		int result = 1;
		val table = this.table;
		int cursor = table.length();
		for (int i = 0; i < cursor; i++) {
			result *= 31;
			result += Objects.hashCode(table.get(i).hashCode());
		}
		return result;
	}

	@Override
	public String toString() {
		return "Locals{" +
				"table=" + Arrays.toString(table.unwrap()) +
				'}';
	}
}
