package dev.xdark.ssvm.value;

/**
 * represents {@code null} value.
 *
 * @author xDark
 */
public final class NullValue implements Value {

	public static final NullValue INSTANCE = new NullValue();

	private NullValue() {
	}

	@Override
	public <T> T as(Class<T> type) {
		return null;
	}

	@Override
	public boolean isWide() {
		return false;
	}

	@Override
	public boolean isUninitialized() {
		return false;
	}

	@Override
	public boolean isNull() {
		return true;
	}
}
