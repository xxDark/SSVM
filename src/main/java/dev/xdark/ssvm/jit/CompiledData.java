package dev.xdark.ssvm.jit;

import java.util.List;

/**
 * Compiled data.
 *
 * @author xDark
 */
public final class CompiledData {
	private final String className;
	private final byte[] bytecode;
	private final List<Object> constants;

	public CompiledData(String className, byte[] bytecode, List<Object> constants) {
		this.className = className;
		this.bytecode = bytecode;
		this.constants = constants;
	}

	/**
	 * @return Class name.
	 */
	public String getClassName() {
		return className;
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

