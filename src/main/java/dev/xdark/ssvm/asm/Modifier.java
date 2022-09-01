package dev.xdark.ssvm.asm;

import lombok.experimental.UtilityClass;
import org.objectweb.asm.Opcodes;

/**
 * VM specific modifiers.
 *
 * @author xDark
 */
@UtilityClass
public class Modifier {

	public static final int RECOGNIZED_CLASS_MODIFIERS = (Opcodes.ACC_PUBLIC |
		Opcodes.ACC_FINAL |
		Opcodes.ACC_SUPER |
		Opcodes.ACC_INTERFACE |
		Opcodes.ACC_ABSTRACT |
		Opcodes.ACC_ANNOTATION |
		Opcodes.ACC_ENUM |
		Opcodes.ACC_SYNTHETIC);
	public static final int RECOGNIZED_FIELD_MODIFIERS = (Opcodes.ACC_PUBLIC |
		Opcodes.ACC_PRIVATE |
		Opcodes.ACC_PROTECTED |
		Opcodes.ACC_STATIC |
		Opcodes.ACC_FINAL |
		Opcodes.ACC_VOLATILE |
		Opcodes.ACC_TRANSIENT |
		Opcodes.ACC_ENUM |
		Opcodes.ACC_SYNTHETIC);
	public static final int RECOGNIZED_METHOD_MODIFIERS = Opcodes.ACC_PUBLIC |
		Opcodes.ACC_PRIVATE |
		Opcodes.ACC_PROTECTED |
		Opcodes.ACC_STATIC |
		Opcodes.ACC_FINAL |
		Opcodes.ACC_SYNCHRONIZED |
		Opcodes.ACC_BRIDGE |
		Opcodes.ACC_VARARGS |
		Opcodes.ACC_NATIVE |
		Opcodes.ACC_ABSTRACT |
		Opcodes.ACC_STRICT |
		Opcodes.ACC_SYNTHETIC;
	public static final int ACC_VM_HIDDEN = 1 << 16;
	public static final int ACC_HIDDEN_FRAME = 1 << 17;
	public static final int ACC_COMPILED = 1 << 18;
	public static final int ACC_CALLER_SENSITIVE = 1 << 19;

	/**
	 * Drops all VM related modifiers.
	 *
	 * @param modifiers Modifiers to drop from.
	 */
	public int eraseClass(int modifiers) {
		return modifiers & RECOGNIZED_CLASS_MODIFIERS;
	}

	/**
	 * Drops all VM related modifiers.
	 *
	 * @param modifiers Modifiers to drop from.
	 */
	public int eraseField(int modifiers) {
		return modifiers & RECOGNIZED_FIELD_MODIFIERS;
	}

	/**
	 * Drops all VM related modifiers.
	 *
	 * @param modifiers Modifiers to drop from.
	 */
	public int eraseMethod(int modifiers) {
		return modifiers & RECOGNIZED_METHOD_MODIFIERS;
	}

	/**
	 * Returns true if the integer argument
	 * includes {@link Modifier#ACC_VM_HIDDEN} modifier.
	 *
	 * @param modifiers A set of modifiers.
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
	 * @param modifiers A set of modifiers.
	 * @return true if the integer argument
	 * includes {@link Modifier#ACC_HIDDEN_FRAME} modifier,
	 * {@code false} otherwise.
	 */
	public boolean isHiddenFrame(int modifiers) {
		return (modifiers & ACC_HIDDEN_FRAME) != 0;
	}

	/**
	 * Returns true if the integer argument
	 * includes {@link Modifier#ACC_COMPILED} modifier.
	 *
	 * @param modifiers A set of modifiers.
	 * @return true if the integer argument
	 * includes {@link Modifier#ACC_COMPILED} modifier,
	 * {@code false} otherwise.
	 */
	public boolean isCompiledMethod(int modifiers) {
		return (modifiers & ACC_COMPILED) != 0;
	}

	/**
	 * Returns true if the integer argument
	 * includes {@link Modifier#ACC_CALLER_SENSITIVE} modifier.
	 *
	 * @param modifiers A set of modifiers.
	 * @return true if the integer argument
	 * includes {@link Modifier#ACC_CALLER_SENSITIVE} modifier,
	 * {@code false} otherwise.
	 */
	public boolean isCallerSensitive(int modifiers) {
		return (modifiers & ACC_CALLER_SENSITIVE) != 0;
	}
}
