package dev.xdark.ssvm.asm;

import lombok.experimental.UtilityClass;

/**
 * Set of opcodes for the VM.
 * For set of JVM reserved opcodes,
 * see {@link JVMOpcodes}.
 *
 * @author xDark
 */
@UtilityClass
public class VMOpcodes {

	public static final int DYNAMIC_CALL = 257;
	public static final int LDC = DYNAMIC_CALL + 1;
}
