package dev.xdark.ssvm.util;

import dev.xdark.ssvm.mirror.JavaMethod;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;

import java.lang.reflect.Field;

/**
 * ASM utilities.
 *
 * @author xDark
 */
@UtilityClass
public class AsmUtil {

	private final Field INSN_INDEX;
	private final String[] INSN_NAMES;

	/**
	 * Returns maximum amount of local variable slots.
	 *
	 * @param mn
	 * 		Method to calculate the amount from.
	 *
	 * @return maximum amount of local variable slots.
	 */
	public int getMaxLocals(JavaMethod mn) {
		int access = mn.getAccess();
		if ((access & Opcodes.ACC_NATIVE) != 0 || (access & Opcodes.ACC_ABSTRACT) != 0) {
			int max = 0;
			if ((access & Opcodes.ACC_STATIC) == 0) {
				max++;
			}
			for (val type : mn.getArgumentTypes()) {
				max += type.getSize();
			}
			return max;
		}
		return mn.getNode().maxLocals;
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
	public int getIndex(AbstractInsnNode insnNode) {
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
	public String getName(int opcode) {
		return INSN_NAMES[opcode];
	}

	/**
	 * Returns default descriptor value.
	 *
	 * @param desc
	 * 		Type descriptor.
	 */
	public Object getDefaultValue(String desc) {
		switch (desc) {
			case "J":
				return 0L;
			case "D":
				return 0.0D;
			case "I":
			case "S":
			case "B":
			case "Z":
			case "C":
				return 0;
			case "F":
				return 0.0F;
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
		val insnNames = new String[Opcodes.IFNONNULL + 1];
		try {
			for (val f : Opcodes.class.getDeclaredFields()) {
				if (f.getType() != int.class) {
					continue;
				}
				val value = f.getInt(null);
				if (value >= Opcodes.NOP && value <= Opcodes.IFNONNULL)
					insnNames[value] = f.getName();
			}
		} catch (IllegalAccessException ex) {
			throw new ExceptionInInitializerError(ex);
		}
		INSN_NAMES = insnNames;
	}
}
