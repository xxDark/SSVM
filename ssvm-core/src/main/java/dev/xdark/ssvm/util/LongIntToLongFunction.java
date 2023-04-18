package dev.xdark.ssvm.util;

/**
 * Takes long, int and produces new long.
 *
 * @author xDark
 */
@FunctionalInterface
public interface LongIntToLongFunction {

	long apply(long v1, int v2);
}
