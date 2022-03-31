package dev.xdark.ssvm.asm;

import lombok.experimental.UtilityClass;

/**
 * Set of opcodes that is reserved 
 * by the JVM.
 * 
 * @author xDark
 */
@UtilityClass
public class JVMOpcodes {

	public static final int IMPDEP1 = 0xfe;
	public static final int IMPDEP2 = 0xff;
	public static final int BREAKPOINT = 0xca;
}
