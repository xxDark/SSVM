package dev.xdark.ssvm.util;

/**
 * Takes two doubles and produces new double.
 *
 * @author xDark
 */
@FunctionalInterface
public interface BiDoubleToDoubleFunction {

	double apply(double v1, double v2);
}
