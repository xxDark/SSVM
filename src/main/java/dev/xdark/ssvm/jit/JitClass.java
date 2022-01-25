package dev.xdark.ssvm.jit;

import lombok.Value;

/**
 * JIT class info.
 */
@Value
public class JitClass {

	String className;
	byte[] code;
}
