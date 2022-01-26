package dev.xdark.ssvm.value;

import lombok.val;

/**
 * VM representation for int value.
 *
 * @author xDark
 */
public final class IntValue extends NumericValue {

	private static final IntValue[] CACHE;
	public static final IntValue ONE;
	public static final IntValue ZERO;
	public static final IntValue M_ONE;

	private final int value;

	/**
	 * @param value
	 * 		Int value.
	 *
	 * @deprecated Only for the VM,
	 * use {@link IntValue#of(int)}
	 * instead.
	 */
	@Deprecated
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
	public boolean asBoolean() {
		return value != 0;
	}

	@Override
	public boolean isWide() {
		return false;
	}

	@Override
	public String toString() {
		return "int(" + value + ")";
	}

	@Override
	public boolean equals(Object other) {
		return other instanceof IntValue && value == ((IntValue) other).value;
	}

	/**
	 * @param value
	 * 		Int value.
	 *
	 * @return VM int value.
	 */
	public static IntValue of(int value) {
		if (value >= LOW && value <= HIGH) {
			return CACHE[value - LOW];
		}
		return new IntValue(value);
	}

	static {
		val cache = new IntValue[(HIGH - LOW) + 1];
		int j = LOW;
		for (int k = 0; k < cache.length; k++)
			cache[k] = new IntValue(j++);
		CACHE = cache;
		ONE = of(1);
		ZERO = of(0);
		M_ONE = of(-1);
	}
}
