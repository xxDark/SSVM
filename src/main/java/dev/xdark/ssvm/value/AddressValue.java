package dev.xdark.ssvm.value;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Value holding insn position for JSR/RET
 * instructions.
 *
 * @author xDark
 */
@RequiredArgsConstructor
public final class AddressValue implements Value {

	@Getter
	private final int position;

	@Override
	public <T> T as(Class<T> type) {
		throw new IllegalArgumentException(type.toString());
	}

	@Override
	public boolean isWide() {
		return false;
	}

	@Override
	public boolean isUninitialized() {
		return false;
	}

	@Override
	public boolean isNull() {
		return false;
	}

	@Override
	public boolean isVoid() {
		return false;
	}
}
