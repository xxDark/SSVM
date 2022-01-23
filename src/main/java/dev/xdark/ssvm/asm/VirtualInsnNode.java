package dev.xdark.ssvm.asm;

/**
 * Interface that holds VM-specific opcode.
 */
public interface VirtualInsnNode {

	/**
	 * @return VM opcode.
	 */
	int getVirtualOpcode();
}
