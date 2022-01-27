package dev.xdark.ssvm.asm;

import lombok.experimental.UtilityClass;
import org.objectweb.asm.Opcodes;

/**
 * Set of opcodes for the VM.
 *
 * @author xDark
 */
@UtilityClass
public class VMOpcodes {

	public static final int DYNAMIC_CALL = Opcodes.IFNONNULL + 1;
	public static final int LDC = DYNAMIC_CALL + 1;
}
