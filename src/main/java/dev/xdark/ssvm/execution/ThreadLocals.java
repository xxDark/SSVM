package dev.xdark.ssvm.execution;

import dev.xdark.ssvm.thread.SimpleThreadStorage;
import dev.xdark.ssvm.thread.ThreadRegion;
import dev.xdark.ssvm.util.Disposable;
import dev.xdark.ssvm.value.TopValue;
import dev.xdark.ssvm.value.Value;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Objects;

/**
 * Storage for local variables
 * that uses thread local storage.
 *
 * @author xDark
 */
@RequiredArgsConstructor
public final class ThreadLocals implements Locals, AutoCloseable, Disposable {

	private final ThreadRegion table;

	/**
	 * @param maxSize The maximum amount of local variables.
	 */
	public ThreadLocals(int maxSize) {
		table = SimpleThreadStorage.threadPush(maxSize);
	}

	@Override
	public void set(int index, Value value) {
		table.set(index, Objects.requireNonNull(value, "value"));
	}

	@Override
	public void setWide(int index, Value value) {
		if (!Objects.requireNonNull(value, "value").isWide()) {
			throw new IllegalStateException("Must use set instead");
		}
		ThreadRegion table = this.table;
		table.set(index, value);
		table.set(index + 1, TopValue.INSTANCE);
	}

	@Override
	public <V extends Value> V load(int index) {
		return (V) table.get(index);
	}

	@Override
	public void copyFrom(Locals locals, int srcOffset, int destOffset, int length) {
		ThreadRegion table = this.table;
		if (table.length() == 0) {
			if (length != 0) {
				throw new IllegalStateException();
			}
			return;
		}
		Value[] array = table.getArray();
		if ((!(locals instanceof ThreadLocals))) {
			Value[] from = locals.getTable();
			System.arraycopy(from, srcOffset, array, destOffset, length);
		} else {
			ThreadLocals tl = (ThreadLocals) locals;
			System.arraycopy(tl.table.getArray(), tl.table.map(srcOffset), array, destOffset, length);
		}
	}

	@Override
	public void copyFrom(Value[] locals, int offset, int length) {
		ThreadRegion table = this.table;
		if (table.length() == 0) {
			if (length != 0) {
				throw new IllegalStateException();
			}
			return;
		}
		System.arraycopy(locals, offset, table.getArray(), table.map(0), length);
	}

	@Override
	public Value[] getTable() {
		return table.unwrap();
	}

	@Override
	public int maxSlots() {
		return table.length();
	}

	@Override
	public void dispose() {
		table.dispose();
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		if (!(o instanceof Locals)) {
			return false;
		}
		Locals other = (Locals) o;
		ThreadRegion table = this.table;
		int length = table.length();
		if (table.length() != other.maxSlots()) {
			return false;
		}
		for (int i = 0; i < length; i++) {
			if (!Objects.equals(table.get(i), other.load(i))) {
				return false;
			}
		}
		return true;
	}

	@Override
	public int hashCode() {
		int result = 1;
		ThreadRegion table = this.table;
		int cursor = table.length();
		for (int i = 0; i < cursor; i++) {
			result *= 31;
			result += Objects.hashCode(table.get(i));
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
