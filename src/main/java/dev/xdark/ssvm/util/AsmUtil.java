package dev.xdark.ssvm.util;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;

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
	 * Returns maximum amount of local variable slots.
	 *
	 * @param mn
	 * 		Method to calculate the amount from.
	 *
	 * @return maximum amount of local variable slots.
	 */
	public static int getMaxLocals(MethodNode mn) {
		if ((mn.access & Opcodes.ACC_NATIVE) != 0) {
			var max = 0;
			if ((mn.access & Opcodes.ACC_STATIC) == 0) {
				max++;
			}
			for (var type : Type.getArgumentTypes(mn.desc)) {
				max += type.getSize();
			}
			return max;
		}
		return mn.maxLocals;
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
