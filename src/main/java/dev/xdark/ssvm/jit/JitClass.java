package dev.xdark.ssvm.jit;

import lombok.Value;

import java.util.List;

/**
 * JIT class info.
 *
 * @author xDark
 */
@Value
public class JitClass {
	/**
	 * JIT class name.
	 */
	String className;
	/**
	 * Class bytecode.
	 */
	byte[] code;
	/**
	 * Compiler constants.
	 */
	List<Object> constants;
}
