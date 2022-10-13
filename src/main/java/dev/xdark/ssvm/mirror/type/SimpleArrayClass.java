package dev.xdark.ssvm.mirror.type;

import dev.xdark.ssvm.LanguageSpecification;
import dev.xdark.ssvm.execution.PanicException;
import dev.xdark.ssvm.mirror.MirrorFactory;
import dev.xdark.ssvm.util.Assertions;
import dev.xdark.ssvm.value.InstanceValue;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public final class SimpleArrayClass implements ArrayClass {

	private final MirrorFactory mirrorFactory;
	private final String internalName;
	private final String name;
	private final JavaClass componentType;
	private final int dimensions;
	private InstanceValue oop;
	private ArrayClass arrayClass;
	private Type type;

	/**
	 * @param mirrorFactory Mirror factory.
	 * @param componentType Component of the array.
	 */
	public SimpleArrayClass(MirrorFactory mirrorFactory, JavaClass componentType) {
		this.mirrorFactory = mirrorFactory;
		String internalName = '[' + componentType.getDescriptor();
		this.internalName = internalName;
		name = internalName.replace('/', '.');
		this.componentType = componentType;
		this.dimensions = (componentType instanceof ArrayClass ? ((ArrayClass) componentType).getDimensions() + 1 : 1);
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
	public InstanceClass[] getInterfaces() {
		return new InstanceClass[0];
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
					arrayClass = mirrorFactory.newArrayClass(this);
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
}
