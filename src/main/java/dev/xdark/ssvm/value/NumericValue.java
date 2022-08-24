package dev.xdark.ssvm.value;

/**
 * Basic VM representation for numeric value, e.g.
 * long, int, double, float, etc.
 *
 * @author xDark
 */
public abstract class NumericValue implements Value {

	// Min & max values for the cache.
	public static final int LOW = Short.MIN_VALUE, HIGH = Short.MAX_VALUE;

	@Override
	public boolean asBoolean() {
		return asByte() != 0;
	}

	@Override
	public boolean isVoid() {
		return false;
	}
}
