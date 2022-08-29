package dev.xdark.ssvm.jit;

import java.util.List;

/**
 * Compiled data.
 *
 * @author xDark
 */
public final class CompiledData {
	private final byte[] bytecode;
	private final List<Object> constants;

	public CompiledData(byte[] bytecode, List<Object> constants) {
		this.bytecode = bytecode;
		this.constants = constants;
	}

	/**
	 * @return compiled bytecode.
	 */
	public byte[] getBytecode() {
		return bytecode;
	}

	/**
	 * @return constants.
	 */
	public List<Object> getConstants() {
		return constants;
	}
}

