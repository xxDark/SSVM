package dev.xdark.ssvm.mirror.type;

import dev.xdark.ssvm.mirror.MirrorFactory;
import dev.xdark.ssvm.util.Assertions;
import dev.xdark.ssvm.value.InstanceValue;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * Java class implementation for long, int, double, float, etc.
 *
 * @author xDark
 */
public final class SimplePrimitiveClass implements PrimitiveClass {

	private final MirrorFactory mirrorFactory;
	private final String name;
	private final String descriptor;
	private final Type type;
	private InstanceValue oop;
	private ArrayClass arrayClass;

	/**
	 * @param mirrorFactory Mirror factory.
	 * @param type          Type.
	 */
	public SimplePrimitiveClass(MirrorFactory mirrorFactory, Type type) {
		this.mirrorFactory = mirrorFactory;
		this.name = type.getClassName();
		this.descriptor = type.getDescriptor();
		this.type = type;
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
	public InstanceValue getOop() {
		return oop;
	}

	@Override
	public InstanceClass[] getInterfaces() {
		return new InstanceClass[0];
	}

	@Override
	public ArrayClass newArrayClass() {
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
		return this == other;
	}

	@Override
	public boolean isPrimitive() {
		return true;
	}

	@Override
	public boolean isArray() {
		return false;
	}

	@Override
	public boolean isInterface() {
		return false;
	}

	@Override
	public JavaClass getComponentType() {
		return null;
	}

	@Override
	public Type getType() {
		return type;
	}

	@Override
	public int getSort() {
		return type.getSort();
	}

	@Override
	public void setOop(InstanceValue oop) {
		Assertions.notNull(oop, "class oop");
		Assertions.isNull(this.oop, "cannot re-assign class oop");
		this.oop = oop;
	}
}
