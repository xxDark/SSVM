package dev.xdark.ssvm.util;

/**
 * Takes two ints and produces boolean.
 *
 * @author xDark
 */
@FunctionalInterface
public interface BiIntPredicate {

	boolean test(int v1, int v2);
}
