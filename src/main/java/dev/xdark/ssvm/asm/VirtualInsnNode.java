package dev.xdark.ssvm.asm;

/**
 * Interface that holds VM-specific opcode.
 *
 * @author xDark
 */
public interface VirtualInsnNode {

	/**
	 * @return VM opcode.
	 */
	int getVirtualOpcode();
}
