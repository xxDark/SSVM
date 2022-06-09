package dev.xdark.ssvm.util;

import lombok.experimental.UtilityClass;

/**
 * Math utilities for some bytecode operations.
 *
 * @author xDark
 */
@UtilityClass
public class MathUtil {

	/**
	 * Compares two doubles.
	 *
	 * @param v1       First value.
	 * @param v2       Second value.
	 * @param nanValue Comparison result if either {@code v1} or {@code v2} is {@code NaN}.
	 * @return comparison result.
	 */
	public int compareDouble(double v1, double v2, int nanValue) {
		if (Double.isNaN(v1) || Double.isNaN(v2)) {
			return nanValue;
		}
		return Double.compare(v1, v2);
	}

	/**
	 * Compares two floats.
	 *
	 * @param v1       First value.
	 * @param v2       Second value.
	 * @param nanValue Comparison result if either {@code v1} or {@code v2} is {@code NaN}.
	 * @return comparison result.
	 */
	public int compareFloat(float v1, float v2, int nanValue) {
		if (Float.isNaN(v1) || Float.isNaN(v2)) {
			return nanValue;
		}
		return Float.compare(v1, v2);
	}
}
