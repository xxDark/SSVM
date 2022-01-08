package dev.xdark.ssvm.mirror;

import dev.xdark.ssvm.value.NullValue;
import dev.xdark.ssvm.value.Value;
import org.objectweb.asm.Opcodes;

/**
 * Java class implementation for long, int, double, float, etc.
 *
 * @author xDark
 */
public final class PrimitiveClass implements JavaClass {

	private final String name;
	private final String descriptor;
	private final Value oop;

	/**
	 * @param name
	 * 		Name of the class.
	 * @param descriptor
	 * 		Descriptor of the class.
	 * @param oop
	 * 		Class oop.
	 */
	public PrimitiveClass(String name, String descriptor, Value oop) {
		this.name = name;
		this.descriptor = descriptor;
		this.oop = oop;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getInternalName() {
		return name;
	}

	@Override
	public String getDescriptor() {
		return descriptor;
	}

	@Override
	public int getModifiers() {
		return Opcodes.ACC_PUBLIC;
	}

	@Override
	public Value getClassLoader() {
		return NullValue.INSTANCE;
	}

	@Override
	public Value getOop() {
		return oop;
	}
}
