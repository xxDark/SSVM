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
}
