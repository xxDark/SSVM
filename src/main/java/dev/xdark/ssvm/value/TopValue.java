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
	public boolean isWide() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isVoid() {
		throw new UnsupportedOperationException();
	}
}
