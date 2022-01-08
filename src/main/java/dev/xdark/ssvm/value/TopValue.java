package dev.xdark.ssvm.value;

/**
 * Internal TOP value.
 *
 * @author xDark
 */
public final class TopValue implements Value {

	public static final TopValue INSTANCE = new TopValue();

	private TopValue() {
	}

	@Override
	public <T> T as(Class<T> type) {
		throw new IllegalStateException();
	}

	@Override
	public boolean isWide() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isUninitialized() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isNull() {
		throw new UnsupportedOperationException();
	}
}
