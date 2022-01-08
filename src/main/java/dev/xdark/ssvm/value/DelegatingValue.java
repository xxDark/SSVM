package dev.xdark.ssvm.value;

/**
 * Acts like a wrapper around delegating value.
 */
public final class DelegatingValue implements Value {

	private Value delegate;

	/**
	 * @param delegate
	 * 		Value to delegate by.
	 */
	public DelegatingValue(Value delegate) {
		this.delegate = delegate;
	}

	public DelegatingValue() {
	}

	@Override
	public <T> T as(Class<T> type) {
		return delegate.as(type);
	}

	@Override
	public long asLong() {
		return delegate.asLong();
	}

	@Override
	public double asDouble() {
		return delegate.asDouble();
	}

	@Override
	public int asInt() {
		return delegate.asInt();
	}

	@Override
	public float asFloat() {
		return delegate.asFloat();
	}

	@Override
	public char asChar() {
		return delegate.asChar();
	}

	@Override
	public short asShort() {
		return delegate.asShort();
	}

	@Override
	public byte asByte() {
		return delegate.asByte();
	}

	@Override
	public boolean asBoolean() {
		return delegate.asBoolean();
	}

	@Override
	public boolean isWide() {
		return delegate.isWide();
	}

	@Override
	public boolean isUninitialized() {
		return delegate.isUninitialized();
	}

	@Override
	public boolean isNull() {
		return delegate.isNull();
	}

	/**
	 * Returns delegating value.
	 *
	 * @return delegating value.
	 */
	public Value getDelegate() {
		return delegate;
	}

	/**
	 * Sets delegating value.
	 *
	 * @param delegate
	 * 		Value to delegate by.
	 */
	public void setDelegate(Value delegate) {
		this.delegate = delegate;
	}
}
