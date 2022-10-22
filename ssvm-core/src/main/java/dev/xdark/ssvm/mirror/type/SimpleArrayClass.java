package dev.xdark.ssvm.mirror.type;

import dev.xdark.jlinker.ClassInfo;
import dev.xdark.ssvm.LanguageSpecification;
import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.execution.PanicException;
import dev.xdark.ssvm.symbol.Symbols;
import dev.xdark.ssvm.util.Assertions;
import dev.xdark.ssvm.value.InstanceValue;
import dev.xdark.ssvm.value.ObjectValue;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.util.Arrays;
import java.util.List;

public final class SimpleArrayClass implements ArrayClass {

	private final VirtualMachine vm;
	private final String internalName;
	private final String name;
	private final JavaClass componentType;
	private final int dimensions;
	private InstanceValue oop;
	private int id = -1;
	private ArrayClass arrayClass;
	private Type type;

	/**
	 * @param vm            VM instance.
	 * @param componentType Component of the array.
	 */
	public SimpleArrayClass(VirtualMachine vm, JavaClass componentType) {
		this.vm = vm;
		String internalName = '[' + componentType.getDescriptor();
		this.internalName = internalName;
		name = internalName.replace('/', '.');
		this.componentType = componentType;
		this.dimensions = (componentType instanceof ArrayClass ? ((ArrayClass) componentType).getDimensions() + 1 : 1);
	}

	@Override
	public VirtualMachine getVM() {
		return vm;
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
	public InstanceValue getOop() {
		return oop;
	}

	@Override
	public int getId() {
		return id;
	}

	@Override
	public ObjectValue getClassLoader() {
		return componentType.getClassLoader();
	}

	@Override
	public InstanceClass getSuperClass() {
		return vm.getSymbols().java_lang_Object();
	}

	@Override
	public List<InstanceClass> getInterfaces() {
		Symbols symbols = vm.getSymbols();
		return Arrays.asList(symbols.java_lang_Cloneable(), symbols.java_io_Serializable());
	}

	@Override
	public int getDimensions() {
		return dimensions;
	}

	@Override
	public ArrayClass newArrayClass() {
		int dimensions = this.dimensions;
		if (dimensions == LanguageSpecification.ARRAY_DIMENSION_LIMIT) {
			throw new PanicException("Too much dimensions");
		}
		ArrayClass arrayClass = this.arrayClass;
		if (arrayClass == null) {
			synchronized (this) {
				arrayClass = this.arrayClass;
				if (arrayClass == null) {
					arrayClass = vm.getMirrorFactory().newArrayClass(this);
					this.arrayClass = arrayClass;
				}
			}
		}
		return arrayClass;
	}

	@Override
	public ArrayClass getArrayClass() {
		return arrayClass;
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
	public Type getType() {
		Type type = this.type;
		if (type == null) {
			type = Type.getType(getDescriptor());
			this.type = type;
		}
		return type;
	}

	@Override
	public int getSort() {
		return Type.ARRAY;
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public void setOop(InstanceValue oop) {
		Assertions.notNull(oop, "class oop");
		Assertions.isNull(this.oop, "cannot re-assign class oop");
		this.oop = oop;
	}

	@Override
	public void setId(int id) {
		Assertions.check(this.id == -1, "id already set");
		this.id = id;
	}

	@Override
	public ClassInfo<JavaClass> linkerInfo() {
		return vm.getSymbols().java_lang_Object().linkerInfo();
	}
}
