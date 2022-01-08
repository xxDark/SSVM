package dev.xdark.ssvm.util;

/**
 * Takes two ints and produces new int.
 *
 * @author xDark
 */
@FunctionalInterface
public interface BiIntToIntFunction {

	int apply(int v1, int v2);
}
