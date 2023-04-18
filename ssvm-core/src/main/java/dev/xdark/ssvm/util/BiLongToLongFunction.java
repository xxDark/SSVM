package dev.xdark.ssvm.util;

/**
 * Takes two longs and produces new long.
 *
 * @author xDark
 */
@FunctionalInterface
public interface BiLongToLongFunction {

	long apply(long v1, long v2);
}
