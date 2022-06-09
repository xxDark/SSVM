package dev.xdark.ssvm.mirror;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.execution.PanicException;
import dev.xdark.ssvm.value.InstanceValue;
import dev.xdark.ssvm.value.ObjectValue;
import org.objectweb.asm.Opcodes;

public final class ArrayJavaClass implements JavaClass {

	private final VirtualMachine vm;
	private final String internalName;
	private final String name;
	private final int dimensions;
	private final JavaClass componentType;
	private final InstanceValue oop;
	private final InstanceJavaClass objectClass;
	private ArrayJavaClass arrayClass;

	/**
	 * @param vm            VM instance.
	 * @param internalName  Internal name of the array class.
	 * @param dimensions    Amount of dimensions.
	 * @param componentType Component of the array.
	 */
	public ArrayJavaClass(VirtualMachine vm, String internalName, int dimensions, JavaClass componentType) {
		this.vm = vm;
		this.internalName = internalName;
		this.dimensions = dimensions;
		this.componentType = componentType;
		name = internalName.replace('/', '.');
		oop = vm.getMemoryManager().createOopForClass(this);
		objectClass = vm.getSymbols().java_lang_Object();
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getInternalName() {
		return internalName;
	}

	@Override
	public String getDescriptor() {
		return internalName;
	}

	@Override
	public int getModifiers() {
		return Opcodes.ACC_PUBLIC;
	}

	@Override
	public ObjectValue getClassLoader() {
		return componentType.getClassLoader();
	}

	@Override
	public InstanceValue getOop() {
		return oop;
	}

	@Override
	public FieldLayout getVirtualFieldLayout() {
		return objectClass.getVirtualFieldLayout();
	}

	@Override
	public FieldLayout getStaticFieldLayout() {
		return FieldLayout.EMPTY;
	}

	@Override
	public InstanceJavaClass getSuperClass() {
		return objectClass;
	}

	@Override
	public InstanceJavaClass[] getInterfaces() {
		return new InstanceJavaClass[0];
	}

	@Override
	public ArrayJavaClass newArrayClass() {
		int dimensions = this.dimensions;
		if (dimensions == 256) {
			throw new PanicException("Too much dimensions");
		}
		ArrayJavaClass arrayClass = this.arrayClass;
		if (arrayClass == null) {
			VirtualMachine vm = this.vm;
			arrayClass = this.arrayClass = new ArrayJavaClass(vm, '[' + name, dimensions + 1, this);
			vm.getHelper().setComponentType(arrayClass, this);
		}
		return arrayClass;
	}

	@Override
	public void initialize() {
	}

	@Override
	public boolean isAssignableFrom(JavaClass other) {
		if (other.isArray()) {
			return componentType.isAssignableFrom(other.getComponentType());
		}
		return false;
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

	@Override
	public JavaClass getComponentType() {
		return componentType;
	}

	@Override
	public String toString() {
		return name;
	}
}
