package dev.xdark.ssvm.util;

import org.objectweb.asm.tree.AbstractInsnNode;

import java.lang.reflect.Field;

/**
 * ASM utilities.
 *
 * @author xDark
 */
public final class AsmUtil {

	private static final Field INSN_INDEX;

	private AsmUtil() {
	}

	/**
	 * Returns index of the instruction.
	 *
	 * @param insnNode
	 * 		Instruction to get index from.
	 *
	 * @return index of the instruction.
	 *
	 * @throws IllegalStateException
	 * 		If index could not be extracted.
	 */
	public static int getIndex(AbstractInsnNode insnNode) {
		try {
			return INSN_INDEX.getInt(insnNode);
		} catch (IllegalAccessException ex) {
			throw new IllegalStateException(ex);
		}
	}

	static {
		try {
			(INSN_INDEX = AbstractInsnNode.class.getDeclaredField("index")).setAccessible(true);
		} catch (NoSuchFieldException ex) {
			throw new ExceptionInInitializerError(ex);
		}
	}
}
