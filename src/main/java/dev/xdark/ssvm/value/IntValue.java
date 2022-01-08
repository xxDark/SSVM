package dev.xdark.ssvm.value;

/**
 * VM representation for int value.
 *
 * @author xDark
 */
public final class IntValue extends NumericValue {

	private final int value;

	public IntValue(int value) {
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
		return value;
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
	public boolean isWide() {
		return false;
	}
}
