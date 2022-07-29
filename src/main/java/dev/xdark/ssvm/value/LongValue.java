package dev.xdark.ssvm.value;

/**
 * VM representation for long value.
 *
 * @author xDark
 */
public final class LongValue extends NumericValue implements WideValue {

	private static final LongValue[] CACHE;
	public static final LongValue ONE;
	public static final LongValue ZERO;
	public static final LongValue M_ONE;

	private final long value;

	/**
	 * @param value Long value.
	 * @deprecated Only for the VM,
	 * use {@link LongValue#of(long)}
	 * instead.
	 */
	@Deprecated
	public LongValue(long value) {
		this.value = value;
	}

	@Override
	public long asLong() {
		return value;
	}

	@Override
	public int asInt() {
		return (int) value;
	}

	@Override
	public String toString() {
		return "long(" + value + ")";
	}

	@Override
	public int hashCode() {
		return Long.hashCode(value);
	}

	@Override
	public boolean equals(Object other) {
		return other instanceof LongValue && value == ((LongValue) other).value;
	}

	/**
	 * @param value InLongt value.
	 * @return VM long value.
	 */
	public static LongValue of(long value) {
		if (value >= LOW && value <= HIGH) {
			return CACHE[(int) value - LOW];
		}
		return new LongValue(value);
	}

	/**
	 * Creates new cache.
	 *
	 * @param low  Min value.
	 * @param high Max value.
	 * @return int cache.
	 */
	public static LongValue[] createCache(int low, int high) {
		LongValue[] cache = new LongValue[(high - low) + 1];
		int j = low;
		for (int k = 0; k < cache.length; k++) {
			cache[k] = new LongValue(j++);
		}
		return cache;
	}

	static {
		CACHE = createCache(LOW, HIGH);
		ONE = of(1);
		ZERO = of(0);
		M_ONE = of(-1);
	}
}
