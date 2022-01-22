package dev.xdark.ssvm.value;

/**
 * VM representation for long value.
 *
 * @author xDark
 */
public final class LongValue extends NumericValue implements WideValue {

	public static final LongValue ONE = new LongValue(0L);
	public static final LongValue ZERO = new LongValue(0L);
	public static final LongValue M_ONE = new LongValue(-1L);

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

	@Override
	public String toString() {
		return "long(" + value + ")";
	}
}
