package dev.xdark.ssvm.mirror;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.value.NullValue;
import dev.xdark.ssvm.value.Value;
import org.objectweb.asm.Opcodes;

/**
 * Java class implementation for long, int, double, float, etc.
 *
 * @author xDark
 */
public final class PrimitiveClass implements JavaClass {

	private final VirtualMachine vm;
	private final String name;
	private final String descriptor;
	private final Value oop;
	private final JavaClass objectClass;

	/**
	 * @param vm
	 * 		VM instance.
	 * @param name
	 * 		Name of the class.
	 * @param descriptor
	 * 		Descriptor of the class.
	 */
	public PrimitiveClass(VirtualMachine vm, String name, String descriptor) {
		this.vm = vm;
		this.name = name;
		this.descriptor = descriptor;
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
		return new ArrayJavaClass(vm, '[' + descriptor, 1, this);
	}

	@Override
	public void initialize() {
	}
}
