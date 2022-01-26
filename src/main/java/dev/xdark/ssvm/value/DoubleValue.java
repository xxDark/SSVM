package dev.xdark.ssvm.value;

/**
 * VM representation for double value.
 *
 * @author xDark
 */
public final class DoubleValue extends NumericValue implements WideValue {

	private final double value;

	public DoubleValue(double value) {
		this.value = value;
	}

	@Override
	public long asLong() {
		return (long) value;
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
		return (float) value;
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

	@Override
	public String toString() {
		return "double(" + value + ")";
	}

	@Override
	public boolean equals(Object other) {
		return other instanceof DoubleValue && value == ((DoubleValue) other).value;
	}
}
