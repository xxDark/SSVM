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
	// Reserved for future optimizations
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
}
