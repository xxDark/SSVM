package dev.xdark.ssvm.value;

import dev.xdark.ssvm.memory.Memory;
import dev.xdark.ssvm.mirror.JavaClass;

public final class ClassValue extends ObjectValue {

	private final JavaClass javaClass;

	/**
	 * @param memory
	 * 		Object data.
	 * @param javaClass
	 * 		An actual class.
	 */
	public ClassValue(Memory memory, JavaClass javaClass) {
		super(memory);
		this.javaClass = javaClass;
	}

	@Override
	public JavaClass getJavaClass() {
		return javaClass;
	}
}
