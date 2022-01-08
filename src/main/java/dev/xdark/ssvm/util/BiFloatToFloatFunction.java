package dev.xdark.ssvm.util;

/**
 * Takes two floats and produces new float.
 *
 * @author xDark
 */
@FunctionalInterface
public interface BiFloatToFloatFunction {

	float apply(float v1, float v2);
}
