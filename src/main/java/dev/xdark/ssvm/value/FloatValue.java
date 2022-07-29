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
		return Float.floatToRawIntBits(value);
	}

	@Override
	public int asInt() {
		return Float.floatToRawIntBits(value);
	}

	@Override
	public float asFloat() {
		return value;
	}

	@Override
	public boolean isWide() {
		return false;
	}

	@Override
	public String toString() {
		return "float(" + value + ")";
	}

	@Override
	public int hashCode() {
		return Float.hashCode(value);
	}

	@Override
	public boolean equals(Object other) {
		return other instanceof FloatValue && value == ((FloatValue) other).value;
	}
}
