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
		return Double.doubleToRawLongBits(value);
	}

	@Override
	public double asDouble() {
		return value;
	}

	@Override
	public String toString() {
		return "double(" + value + ")";
	}

	@Override
	public int hashCode() {
		return Double.hashCode(value);
	}

	@Override
	public boolean equals(Object other) {
		return other instanceof DoubleValue && value == ((DoubleValue) other).value;
	}
}
