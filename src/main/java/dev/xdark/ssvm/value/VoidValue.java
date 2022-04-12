package dev.xdark.ssvm.value;

/**
 * Represents void value.
 *
 * @author xDark
 */
public final class VoidValue implements Value {

	public static final VoidValue INSTANCE = new VoidValue();

	private VoidValue() {
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

	@Override
	public boolean isVoid() {
		return true;
	}
}
