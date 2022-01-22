package dev.xdark.ssvm.value;

/**
 * VM representation for float value.
 *
 * @author xDark
 */
public final class FloatValue extends NumericValue {

	private final float value;

	public FloatValue(float value) {
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

	@Override
	public boolean isWide() {
		return false;
	}

	@Override
	public String toString() {
		return "float(" + value + ")";
	}
}
