package dev.xdark.ssvm.asm;

import lombok.experimental.UtilityClass;

/**
 * VM specific modifiers.
 *
 * @author xDark
 */
@UtilityClass
public class Modifier {

	public static final int ACC_VM_HIDDEN = 1 << 16;
	public static final int ACC_HIDDEN_FRAME = 1 << 17;

	/**
	 * Drops all VM related modifiers.
	 *
	 * @param modifiers
	 * 		Modifiers to drop from.
	 */
	public int erase(int modifiers) {
		return modifiers & ~(ACC_VM_HIDDEN | ACC_HIDDEN_FRAME);
	}

	/**
	 * Returns true if the integer argument
	 * includes {@link Modifier#ACC_VM_HIDDEN} modifier.
	 *
	 * @param modifiers
	 * 		A set of modifiers.
	 *
	 * @return true if the integer argument
	 * includes {@link Modifier#ACC_VM_HIDDEN} modifier,
	 * {@code false} otherwise.
	 */
	public boolean isHiddenMember(int modifiers) {
		return (modifiers & ACC_VM_HIDDEN) != 0;
	}

	/**
	 * Returns true if the integer argument
	 * includes {@link Modifier#ACC_HIDDEN_FRAME} modifier.
	 *
	 * @param modifiers
	 * 		A set of modifiers.
	 *
	 * @return true if the integer argument
	 * includes {@link Modifier#ACC_HIDDEN_FRAME} modifier,
	 * {@code false} otherwise.
	 */
	public boolean isHiddenFrame(int modifiers) {
		return (modifiers & ACC_HIDDEN_FRAME) != 0;
	}
}
