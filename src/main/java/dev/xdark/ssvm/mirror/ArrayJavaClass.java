package dev.xdark.ssvm.mirror;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.value.Value;
import org.objectweb.asm.Opcodes;

public final class ArrayJavaClass implements JavaClass {

	private final VirtualMachine vm;
	private final String name;
	private final int dimensions;
	private final JavaClass componentType;
	private final Value oop;
	private final JavaClass objectClass;

	/**
	 * @param vm
	 * 		VM instance.
	 * @param name
	 * 		Name of the array class.
	 * @param dimensions
	 * 		Amount of dimensions.
	 * @param componentType
	 * 		Component of the array.
	 */
	public ArrayJavaClass(VirtualMachine vm, String name, int dimensions, JavaClass componentType) {
		this.vm = vm;
		this.name = name;
		this.dimensions = dimensions;
		this.componentType = componentType;
		oop = vm.getMemoryManager().newOopForClass(this);
		objectClass = vm.getSymbols().java_lang_Object;
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
		return name;
	}

	@Override
	public int getModifiers() {
		return Opcodes.ACC_PUBLIC;
	}

	@Override
	public Value getClassLoader() {
		return componentType.getClassLoader();
	}

	@Override
	public Value getOop() {
		return oop;
	}

	@Override
	public ClassLayout getLayout() {
		return objectClass.getLayout();
	}

	@Override
	public JavaClass getSuperClass() {
		return objectClass;
	}

	@Override
	public JavaClass[] getInterfaces() {
		return new JavaClass[0];
	}

	@Override
	public ArrayJavaClass newArrayClass() {
		int dimensions = this.dimensions;
		if (dimensions == 256) {
			throw new IllegalStateException();
		}
		return new ArrayJavaClass(vm, '[' + name, dimensions + 1, this);
	}

	@Override
	public void initialize() {
	}

	@Override
	public boolean isAssignableFrom(JavaClass other) {
		return componentType.isAssignableFrom(other);
	}

	@Override
	public boolean isPrimitive() {
		return false;
	}

	@Override
	public boolean isArray() {
		return true;
	}

	@Override
	public boolean isInterface() {
		return false;
	}
}
