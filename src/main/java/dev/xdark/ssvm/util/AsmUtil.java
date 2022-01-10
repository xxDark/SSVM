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
	private static final String[] INSN_NAMES;

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
		if ((mn.access & Opcodes.ACC_NATIVE) != 0 || (mn.access & Opcodes.ACC_ABSTRACT) != 0) {
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

	/**
	 * Returns opcode name.
	 *
	 * @param opcode
	 * 		Opcode to get name from.
	 *
	 * @return opcode name.
	 */
	public static String getName(int opcode) {
		return INSN_NAMES[opcode];
	}

	/**
	 * Returns default descriptor value.
	 *
	 * @param desc
	 * 		Type descriptor.
	 */
	public static Object getDefaultValue(String desc) {
		switch (desc) {
			case "J":
				return 0L;
			case "D":
				return 0.0D;
			case "I":
			case "S":
			case "B":
			case "Z":
				return 0;
			case "F":
				return 0.0F;
			case "C":
				return '\0';
			default:
				return null;
		}
	}

	static {
		try {
			(INSN_INDEX = AbstractInsnNode.class.getDeclaredField("index")).setAccessible(true);
		} catch (NoSuchFieldException ex) {
			throw new ExceptionInInitializerError(ex);
		}
		var insnNames = new String[Opcodes.IFNONNULL + 1];
		try {
			for (var f : Opcodes.class.getDeclaredFields()) {
				if (f.getType() != int.class) {
					continue;
				}
				var value = f.getInt(null);
				if (value >= Opcodes.NOP && value <= Opcodes.IFNONNULL)
					insnNames[value] = f.getName();
			}
		} catch (IllegalAccessException ex) {
			throw new ExceptionInInitializerError(ex);
		}
		INSN_NAMES = insnNames;
	}
}
