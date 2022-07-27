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
	public static final int VM_LDC = VM_DYNAMIC_CALL + 1;
	public static final int VM_INVOKEVIRTUAL = VM_LDC + 1;
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
}
