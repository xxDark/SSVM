package dev.xdark.ssvm.util;

import dev.xdark.ssvm.mirror.JavaMethod;
import lombok.experimental.UtilityClass;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnNode;
import sun.misc.Unsafe;

import java.lang.reflect.Field;

/**
 * ASM utilities.
 *
 * @author xDark
 */
@UtilityClass
public class AsmUtil {

	private final Long DEFAULT_LONG = 0L;
	private final Double DEFAULT_DOUBLE = 0.0D;
	private final Integer DEFAULT_INT = 0;
	private final Float DEFAULT_FLOAT = 0.0F;
	private final Unsafe UNSAFE;
	private final long INSN_INDEX;
	private final String[] INSN_NAMES;

	/**
	 * Normalizes descriptors.
	 * {@code Ljava/lang/Class; => java/lang/Class}.
	 * {@code [Ljava/lang/String; => identity}.
	 *
	 * @param descriptor Descriptor to normalize.
	 * @return Normalized descriptor.
	 */
	public String normalizeDescriptor(String descriptor) {
		// It seems like JVM can pass descriptors instead of internal names?
		if (!descriptor.isEmpty() && descriptor.charAt(0) == 'L' && descriptor.charAt(descriptor.length() - 1) == ';') {
			return descriptor.substring(1, descriptor.length() - 1);
		}
		return descriptor;
	}

	/**
	 * Returns maximum amount of local variable slots.
	 *
	 * @param mn Method to calculate the amount from.
	 * @return maximum amount of local variable slots.
	 */
	public int getMaxLocals(JavaMethod mn) {
		int access = mn.getModifiers();
		if ((access & Opcodes.ACC_NATIVE) != 0 || (access & Opcodes.ACC_ABSTRACT) != 0) {
			return mn.getMaxArgs();
		}
		return mn.getNode().maxLocals;
	}

	/**
	 * Returns index of the instruction.
	 *
	 * @param insnNode Instruction to get index from.
	 * @return index of the instruction.
	 * @throws IllegalStateException If index could not be extracted.
	 */
	public int getIndex(AbstractInsnNode insnNode) {
		return UNSAFE.getInt(insnNode, INSN_INDEX);
	}

	/**
	 * @param insnNode Instruction to check.
	 * @return {@code true} if instruction is valid.
	 */
	public boolean isValid(AbstractInsnNode insnNode) {
		return getIndex(insnNode) != -1;
	}

	/**
	 * Returns opcode name.
	 *
	 * @param opcode Opcode to get name from.
	 * @return opcode name.
	 */
	public String getName(int opcode) {
		return INSN_NAMES[opcode];
	}

	/**
	 * Returns default descriptor value.
	 *
	 * @param desc Type descriptor.
	 */
	public Object getDefaultValue(String desc) {
		switch (desc) {
			case "J":
				return DEFAULT_LONG;
			case "D":
				return DEFAULT_DOUBLE;
			case "I":
			case "S":
			case "B":
			case "Z":
			case "C":
				return DEFAULT_INT;
			case "F":
				return DEFAULT_FLOAT;
			default:
				return null;
		}
	}

	static {
		Unsafe unsafe = UnsafeUtil.get();
		UNSAFE = unsafe;
		try {
			new InsnNode(0);
			INSN_INDEX = unsafe.objectFieldOffset(AbstractInsnNode.class.getDeclaredField("index"));
		} catch (NoSuchFieldException ex) {
			throw new ExceptionInInitializerError(ex);
		}
		String[] insnNames = new String[Opcodes.IFNONNULL + 1];
		try {
			for (Field f : Opcodes.class.getDeclaredFields()) {
				if (f.getType() != int.class) {
					continue;
				}
				int value = f.getInt(null);
				if (value >= Opcodes.NOP && value <= Opcodes.IFNONNULL) {
					insnNames[value] = f.getName();
				}
			}
		} catch (IllegalAccessException ex) {
			throw new ExceptionInInitializerError(ex);
		}
		INSN_NAMES = insnNames;
	}
}
