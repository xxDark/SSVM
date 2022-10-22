package dev.xdark.ssvm.mirror.type;

import dev.xdark.jlinker.ClassInfo;
import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.util.Assertions;
import dev.xdark.ssvm.value.InstanceValue;
import dev.xdark.ssvm.value.ObjectValue;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.util.Collections;
import java.util.List;

/**
 * Java class implementation for long, int, double, float, etc.
 *
 * @author xDark
 */
public final class SimplePrimitiveClass implements PrimitiveClass {

	private final VirtualMachine vm;
	private final String name;
	private final String descriptor;
	private final Type type;
	private InstanceValue oop;
	private int id = -1;
	private ArrayClass arrayClass;

	/**
	 * @param vm   VM instance.
	 * @param type Type.
	 */
	public SimplePrimitiveClass(VirtualMachine vm, Type type) {
		this.vm = vm;
		this.name = type.getClassName();
		this.descriptor = type.getDescriptor();
		this.type = type;
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
	public int getId() {
		return id;
	}

	@Override
	public ObjectValue getClassLoader() {
		return vm.getMemoryManager().nullValue();
	}

	@Override
	public InstanceClass getSuperClass() {
		return vm.getSymbols().java_lang_Object();
	}

	@Override
	public List<InstanceClass> getInterfaces() {
		return Collections.emptyList();
	}

	@Override
	public ArrayClass newArrayClass() {
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

	@Override
	public void setId(int id) {
		Assertions.check(this.id == -1 , "id already set");
		this.id = id;
	}

	@Override
	public ClassInfo<JavaClass> linkerInfo() {
		throw new UnsupportedOperationException();
	}
}
