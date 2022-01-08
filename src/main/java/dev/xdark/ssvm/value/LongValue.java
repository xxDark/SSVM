package dev.xdark.ssvm.value;

/**
 * VM representation for long value.
 *
 * @author xDark
 */
public final class LongValue extends NumericValue implements WideValue {

	private final long value;

	public LongValue(long value) {
		this.value = value;
	}

	@Override
	public long asLong() {
		return value;
	}

	@Override
	public double asDouble() {
		return value;
	}

	@Override
	public int asInt() {
		return (int) value;
	}

	@Override
	public float asFloat() {
		return value;
	}

	@Override
	public char asChar() {
		return (char) value;
	}

	@Override
	public short asShort() {
		return (short) value;
	}

	@Override
	public byte asByte() {
		return (byte) value;
	}
}
