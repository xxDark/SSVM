package dev.xdark.ssvm.asm;

import lombok.experimental.UtilityClass;

/**
 * Set of opcodes for the VM.
 *
 * @author xDark
 */
@UtilityClass
public class VMOpcodes {

	public static final int DYNAMIC_CALL = 257;
	public static final int LDC = DYNAMIC_CALL + 1;
	// Reserved for future optimizations
	public static final int INVOKEVIRTUAL = LDC + 1;
	public static final int INVOKESPECIAL = INVOKEVIRTUAL + 1;
	public static final int INVOKESTATIC = INVOKESPECIAL + 1;
	public static final int INVOKEINTERFACE = INVOKESTATIC + 1;
	public static final int NEW = INVOKEINTERFACE + 1;
	public static final int ANEWARRAY = NEW + 1;
	public static final int BOOLEAN_NEW_ARRAY = ANEWARRAY + 1;
	public static final int CHAR_NEW_ARRAY = BOOLEAN_NEW_ARRAY + 1;
	public static final int FLOAT_NEW_ARRAY = CHAR_NEW_ARRAY + 1;
	public static final int DOUBLE_NEW_ARRAY = FLOAT_NEW_ARRAY + 1;
	public static final int BYTE_NEW_ARRAY = DOUBLE_NEW_ARRAY + 1;
	public static final int SHORT_NEW_ARRAY = BYTE_NEW_ARRAY + 1;
	public static final int INT_NEW_ARRAY = SHORT_NEW_ARRAY + 1;
	public static final int LONG_NEW_ARRAY = INT_NEW_ARRAY + 1;
	public static final int REFERENCE_NEW_ARRAY = LONG_NEW_ARRAY + 1;
}
