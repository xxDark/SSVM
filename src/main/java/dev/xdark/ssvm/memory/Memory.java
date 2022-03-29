package dev.xdark.ssvm.memory;

import java.nio.ByteBuffer;

/**
 * Represents allocated memory block.
 *
 * @author xDark
 */
public interface Memory {

	/**
	 * Returns manager which allocated this block.
	 *
	 * @return manager which allocated this block.
	 */
	MemoryManager getMemoryManager();

	/**
	 * Returns memory data.
	 *
	 * @return memory data.
	 */
	MemoryData getData();

	/**
	 * Returns memory address.
	 *
	 * @return memory address.
	 */
	long getAddress();

	/**
	 * Returns whether memory is direct not.
	 *
	 * @return {@code true} if memory is direct, {@code false}
	 * otherwise.
	 */
	boolean isDirect();
}
