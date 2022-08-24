package dev.xdark.ssvm.asm;

import lombok.experimental.UtilityClass;

/**
 * Set of opcodes for the VM.
 *
 * @author xDark
 */
@UtilityClass
public class VMOpcodes {

	public static final int VM_DYNAMIC_CALL = 257;
	public static final int VM_INVOKEVIRTUAL = VM_DYNAMIC_CALL + 1;
	public static final int VM_INVOKESPECIAL = VM_INVOKEVIRTUAL + 1;
	public static final int VM_INVOKESTATIC = VM_INVOKESPECIAL + 1;
	public static final int VM_INVOKEINTERFACE = VM_INVOKESTATIC + 1;
	public static final int VM_NEW = VM_INVOKEINTERFACE + 1;
	public static final int VM_ANEWARRAY = VM_NEW + 1;
	public static final int VM_BOOLEAN_NEW_ARRAY = VM_ANEWARRAY + 1;
	public static final int VM_CHAR_NEW_ARRAY = VM_BOOLEAN_NEW_ARRAY + 1;
	public static final int VM_FLOAT_NEW_ARRAY = VM_CHAR_NEW_ARRAY + 1;
	public static final int VM_DOUBLE_NEW_ARRAY = VM_FLOAT_NEW_ARRAY + 1;
	public static final int VM_BYTE_NEW_ARRAY = VM_DOUBLE_NEW_ARRAY + 1;
	public static final int VM_SHORT_NEW_ARRAY = VM_BYTE_NEW_ARRAY + 1;
	public static final int VM_INT_NEW_ARRAY = VM_SHORT_NEW_ARRAY + 1;
	public static final int VM_LONG_NEW_ARRAY = VM_INT_NEW_ARRAY + 1;
	public static final int VM_REFERENCE_NEW_ARRAY = VM_LONG_NEW_ARRAY + 1;
	public static final int VM_CHECKCAST = VM_REFERENCE_NEW_ARRAY + 1;
	public static final int VM_GETSTATIC_BOOLEAN = VM_CHECKCAST + 1;
	public static final int VM_GETSTATIC_CHAR = VM_GETSTATIC_BOOLEAN + 1;
	public static final int VM_GETSTATIC_BYTE = VM_GETSTATIC_CHAR + 1;
	public static final int VM_GETSTATIC_SHORT = VM_GETSTATIC_BYTE + 1;
	public static final int VM_GETSTATIC_INT = VM_GETSTATIC_SHORT + 1;
	public static final int VM_GETSTATIC_FLOAT = VM_GETSTATIC_INT + 1;
	public static final int VM_GETSTATIC_LONG = VM_GETSTATIC_FLOAT + 1;
	public static final int VM_GETSTATIC_DOUBLE = VM_GETSTATIC_LONG + 1;
	public static final int VM_GETSTATIC_REFERENCE = VM_GETSTATIC_DOUBLE + 1;
	public static final int VM_PUTSTATIC_BOOLEAN = VM_GETSTATIC_REFERENCE + 1;
	public static final int VM_PUTSTATIC_CHAR = VM_PUTSTATIC_BOOLEAN + 1;
	public static final int VM_PUTSTATIC_BYTE = VM_PUTSTATIC_CHAR + 1;
	public static final int VM_PUTSTATIC_SHORT = VM_PUTSTATIC_BYTE + 1;
	public static final int VM_PUTSTATIC_INT = VM_PUTSTATIC_SHORT + 1;
	public static final int VM_PUTSTATIC_FLOAT = VM_PUTSTATIC_INT + 1;
	public static final int VM_PUTSTATIC_LONG = VM_PUTSTATIC_FLOAT + 1;
	public static final int VM_PUTSTATIC_DOUBLE = VM_PUTSTATIC_LONG + 1;
	public static final int VM_PUTSTATIC_REFERENCE = VM_PUTSTATIC_DOUBLE + 1;
	public static final int VM_CONSTANT_INT = VM_PUTSTATIC_REFERENCE + 1;
	public static final int VM_CONSTANT_FLOAT = VM_CONSTANT_INT + 1;
	public static final int VM_CONSTANT_LONG = VM_CONSTANT_FLOAT + 1;
	public static final int VM_CONSTANT_DOUBLE = VM_CONSTANT_LONG + 1;
	public static final int VM_CONSTANT_REFERENCE = VM_CONSTANT_DOUBLE + 1;
	public static final int VM_PUTFIELD_BOOLEAN = VM_CONSTANT_REFERENCE + 1;
	public static final int VM_PUTFIELD_CHAR = VM_PUTFIELD_BOOLEAN + 1;
	public static final int VM_PUTFIELD_BYTE = VM_PUTFIELD_CHAR + 1;
	public static final int VM_PUTFIELD_SHORT = VM_PUTFIELD_BYTE + 1;
	public static final int VM_PUTFIELD_INT = VM_PUTFIELD_SHORT + 1;
	public static final int VM_PUTFIELD_FLOAT = VM_PUTFIELD_INT + 1;
	public static final int VM_PUTFIELD_LONG = VM_PUTFIELD_FLOAT + 1;
	public static final int VM_PUTFIELD_DOUBLE = VM_PUTFIELD_LONG + 1;
	public static final int VM_PUTFIELD_REFERENCE = VM_PUTFIELD_DOUBLE + 1;
}
