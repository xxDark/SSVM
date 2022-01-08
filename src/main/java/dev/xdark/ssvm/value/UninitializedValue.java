package dev.xdark.ssvm.value;

/**
 * Represents uninitialized value.
 *
 * @author xDark
 */
public final class UninitializedValue implements Value {

	@Override
	public <T> T as(Class<T> type) {
		throw new IllegalStateException("Uninitialized value cannot be represented as any type");
	}

	@Override
	public boolean isWide() {
		return false;
	}

	@Override
	public boolean isUninitialized() {
		return true;
	}

	@Override
	public boolean isNull() {
		return false;
	}
}
