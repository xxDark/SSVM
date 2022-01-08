package dev.xdark.ssvm.value;

/**
 * Basic interface for all wide values.
 *
 * @author xDark
 */
public interface WideValue extends Value {

	@Override
	default boolean isWide() {
		return true;
	}
}
