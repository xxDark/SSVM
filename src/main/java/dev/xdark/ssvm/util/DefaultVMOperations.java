package dev.xdark.ssvm.util;

import dev.xdark.ssvm.LinkResolver;
import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.execution.VMException;
import dev.xdark.ssvm.memory.management.MemoryManager;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.mirror.JavaClass;
import dev.xdark.ssvm.mirror.JavaField;
import dev.xdark.ssvm.symbol.VMPrimitives;
import dev.xdark.ssvm.symbol.VMSymbols;
import dev.xdark.ssvm.value.ArrayValue;
import dev.xdark.ssvm.value.InstanceValue;
import dev.xdark.ssvm.value.ObjectValue;

/**
 * Some VM operations implementations.
 *
 * @author xDArk
 */
public final class DefaultVMOperations implements VMOperations {

	private final VMSymbols symbols;
	private final VMPrimitives primitives;
	private final VMHelper helper;
	private final MemoryManager memoryManager;
	private final LinkResolver linkResolver;

	/**
	 * @param vm      VM instance.
	 * @param trusted Whether the lookup mode for link resolver is trusted.
	 */
	public DefaultVMOperations(VirtualMachine vm, boolean trusted) {
		symbols = vm.getSymbols();
		primitives = vm.getPrimitives();
		helper = vm.getHelper();
		memoryManager = vm.getMemoryManager();
		linkResolver = trusted ? vm.getTrustedLinkResolver() : vm.getPublicLinkResolver();
	}

	//<editor-fold desc="Allocation methods">
	@Override
	public InstanceValue allocateInstance(JavaClass klass) {
		InstanceJavaClass jc;
		if (!(klass instanceof InstanceJavaClass) || !(jc = (InstanceJavaClass) klass).canAllocateInstance()) {
			helper.throwException(symbols.java_lang_InstantiationError());
			return null;
		}
		jc.initialize();
		return memoryManager.newInstance(jc);
	}

	@Override
	public ArrayValue allocateArray(JavaClass componentType, int length) {
		VMHelper helper = this.helper;
		helper.checkArrayLength(length);
		return helper.newArray(componentType, length);
	}

	@Override
	public ArrayValue allocateLongArray(int length) {
		return allocateArray(primitives.longPrimitive(), length);
	}

	@Override
	public ArrayValue allocateDoubleArray(int length) {
		return allocateArray(primitives.doublePrimitive(), length);
	}

	@Override
	public ArrayValue allocateIntArray(int length) {
		return allocateArray(primitives.intPrimitive(), length);
	}

	@Override
	public ArrayValue allocateFloatArray(int length) {
		return allocateArray(primitives.floatPrimitive(), length);
	}

	@Override
	public ArrayValue allocateCharArray(int length) {
		return allocateArray(primitives.charPrimitive(), length);
	}

	@Override
	public ArrayValue allocateShortArray(int length) {
		return allocateArray(primitives.shortPrimitive(), length);
	}

	@Override
	public ArrayValue allocateByteArray(int length) {
		return allocateArray(primitives.bytePrimitive(), length);
	}

	@Override
	public ArrayValue allocateBooleanArray(int length) {
		return allocateArray(primitives.booleanPrimitive(), length);
	}

	@Override
	public int getArrayLength(ObjectValue value) {
		return helper.checkNotNullArray(value).getLength();
	}
	//</editor-fold>

	//<editor-fold desc="Array load methods">

	@Override
	public ObjectValue arrayLoadReference(ObjectValue array, int index) {
		return verifyArrayAccess(array, index).getReference(index);
	}

	@Override
	public long arrayLoadLong(ObjectValue array, int index) {
		return verifyArrayAccess(array, index).getLong(index);
	}

	@Override
	public double arrayLoadDouble(ObjectValue array, int index) {
		return verifyArrayAccess(array, index).getDouble(index);
	}

	@Override
	public int arrayLoadInt(ObjectValue array, int index) {
		return verifyArrayAccess(array, index).getInt(index);
	}

	@Override
	public float arrayLoadFloat(ObjectValue array, int index) {
		return verifyArrayAccess(array, index).getFloat(index);
	}

	@Override
	public char arrayLoadChar(ObjectValue array, int index) {
		return verifyArrayAccess(array, index).getChar(index);
	}

	@Override
	public short arrayLoadShort(ObjectValue array, int index) {
		return verifyArrayAccess(array, index).getShort(index);
	}

	@Override
	public byte arrayLoadByte(ObjectValue array, int index) {
		return verifyArrayAccess(array, index).getByte(index);
	}

	@Override
	public boolean arrayLoadBoolean(ObjectValue array, int index) {
		return verifyArrayAccess(array, index).getBoolean(index);
	}
	//</editor-fold>

	//<editor-fold desc="Array store methods">

	@Override
	public void arrayStoreReference(ObjectValue array, int index, ObjectValue value) {
		ArrayValue arrayValue = verifyArrayAccess(array, index);
		if (!value.isNull()) {
			JavaClass type = arrayValue.getJavaClass().getComponentType();
			JavaClass valueType = value.getJavaClass();
			if (!type.isAssignableFrom(valueType)) {
				helper.throwException(symbols.java_lang_ArrayStoreException(), valueType.getName());
			}
		}
		arrayValue.setReference(index, value);
	}

	@Override
	public void arrayStoreLong(ObjectValue array, int index, long value) {
		verifyArrayAccess(array, index).setLong(index, value);
	}

	@Override
	public void arrayStoreDouble(ObjectValue array, int index, double value) {
		verifyArrayAccess(array, index).setDouble(index, value);
	}

	@Override
	public void arrayStoreInt(ObjectValue array, int index, int value) {
		verifyArrayAccess(array, index).setInt(index, value);
	}

	@Override
	public void arrayStoreFloat(ObjectValue array, int index, float value) {
		verifyArrayAccess(array, index).setFloat(index, value);
	}

	@Override
	public void arrayStoreChar(ObjectValue array, int index, char value) {
		verifyArrayAccess(array, index).setChar(index, value);
	}

	@Override
	public void arrayStoreShort(ObjectValue array, int index, short value) {
		verifyArrayAccess(array, index).setShort(index, value);
	}

	@Override
	public void arrayStoreByte(ObjectValue array, int index, byte value) {
		verifyArrayAccess(array, index).setByte(index, value);
	}
	//</editor-fold>

	@Override
	public ObjectValue checkCast(ObjectValue value, JavaClass klass) {
		if (!value.isNull()) {
			JavaClass against = value.getJavaClass();
			if (!klass.isAssignableFrom(against)) {
				helper.throwException(symbols.java_lang_ClassCastException(), against.getName() + " cannot be cast to " + klass.getName());
			}
		}
		return value;
	}

	@Override
	public void throwException(ObjectValue value) {
		if (value.isNull()) {
			// NPE it is then.
			value = helper.newException(symbols.java_lang_NullPointerException());
		}
		throw new VMException((InstanceValue) value);
	}

	//<editor-fold desc="Virtual field put methods">
	@Override
	public void putReference(ObjectValue instance, InstanceJavaClass klass, String name, String desc, ObjectValue value) {
		long offset = getFieldOffsetForInstance(instance, klass, name, desc);
		memoryManager.writeValue(instance, offset, value);
	}

	@Override
	public void putReference(ObjectValue instance, String name, String desc, ObjectValue value) {
		putReference(instance, helper.<InstanceValue>checkNotNull(instance).getJavaClass(), name, desc, value);
	}

	@Override
	public void putLong(ObjectValue instance, InstanceJavaClass klass, String name, long value) {
		long offset = getFieldOffsetForInstance(instance, klass, name, "J");
		instance.getData().writeLong(offset, value);
	}

	@Override
	public void putLong(ObjectValue instance, String name, long value) {
		putLong(instance, helper.<InstanceValue>checkNotNull(instance).getJavaClass(), name, value);
	}

	@Override
	public void putDouble(ObjectValue instance, InstanceJavaClass klass, String name, double value) {
		long offset = getFieldOffsetForInstance(instance, klass, name, "D");
		instance.getData().writeLong(offset, Double.doubleToRawLongBits(value));
	}

	@Override
	public void putDouble(ObjectValue instance, String name, double value) {
		putDouble(instance, helper.<InstanceValue>checkNotNull(instance).getJavaClass(), name, value);
	}

	@Override
	public void putInt(ObjectValue instance, InstanceJavaClass klass, String name, int value) {
		long offset = getFieldOffsetForInstance(instance, klass, name, "I");
		instance.getData().writeInt(offset, value);
	}

	@Override
	public void putInt(ObjectValue instance, String name, int value) {
		putInt(instance, helper.<InstanceValue>checkNotNull(instance).getJavaClass(), name, value);
	}

	@Override
	public void putFloat(ObjectValue instance, InstanceJavaClass klass, String name, float value) {
		long offset = getFieldOffsetForInstance(instance, klass, name, "F");
		instance.getData().writeInt(offset, Float.floatToRawIntBits(value));
	}

	@Override
	public void putFloat(ObjectValue instance, String name, float value) {
		putFloat(instance, helper.<InstanceValue>checkNotNull(instance).getJavaClass(), name, value);
	}

	@Override
	public void putChar(ObjectValue instance, InstanceJavaClass klass, String name, char value) {
		long offset = getFieldOffsetForInstance(instance, klass, name, "C");
		instance.getData().writeChar(offset, value);
	}

	@Override
	public void putChar(ObjectValue instance, String name, char value) {
		putChar(instance, helper.<InstanceValue>checkNotNull(instance).getJavaClass(), name, value);
	}

	@Override
	public void putShort(ObjectValue instance, InstanceJavaClass klass, String name, short value) {
		long offset = getFieldOffsetForInstance(instance, klass, name, "S");
		instance.getData().writeShort(offset, value);
	}

	@Override
	public void putShort(ObjectValue instance, String name, short value) {
		putShort(instance, helper.<InstanceValue>checkNotNull(instance).getJavaClass(), name, value);
	}

	@Override
	public void putByte(ObjectValue instance, InstanceJavaClass klass, String name, byte value) {
		long offset = getFieldOffsetForInstance(instance, klass, name, "B");
		instance.getData().writeByte(offset, value);
	}

	@Override
	public void putByte(ObjectValue instance, String name, byte value) {
		putByte(instance, helper.<InstanceValue>checkNotNull(instance).getJavaClass(), name, value);
	}

	@Override
	public void putBoolean(ObjectValue instance, InstanceJavaClass klass, String name, boolean value) {
		long offset = getFieldOffsetForInstance(instance, klass, name, "Z");
		instance.getData().writeByte(offset, (byte) (value ? 1 : 0));
	}

	@Override
	public void putBoolean(ObjectValue instance, String name, boolean value) {
		putBoolean(instance, helper.<InstanceValue>checkNotNull(instance).getJavaClass(), name, value);
	}
	//</editor-fold>

	//<editor-fold desc="Virtual field get methods">
	@Override
	public ObjectValue getReference(ObjectValue instance, InstanceJavaClass klass, String name, String desc) {
		long offset = getFieldOffsetForInstance(instance, klass, name, desc);
		return memoryManager.readReference(instance, offset);
	}

	@Override
	public ObjectValue getReference(ObjectValue instance, String name, String desc) {
		return getReference(instance, helper.<InstanceValue>checkNotNull(instance).getJavaClass(), name, desc);
	}

	@Override
	public long getLong(ObjectValue instance, InstanceJavaClass klass, String name) {
		long offset = getFieldOffsetForInstance(instance, klass, name, "J");
		return instance.getData().readLong(offset);
	}

	@Override
	public long getLong(ObjectValue instance, String name) {
		return getLong(instance, helper.<InstanceValue>checkNotNull(instance).getJavaClass(), name);
	}

	@Override
	public double getDouble(ObjectValue instance, InstanceJavaClass klass, String name) {
		long offset = getFieldOffsetForInstance(instance, klass, name, "D");
		return Double.longBitsToDouble(instance.getData().readLong(offset));
	}

	@Override
	public double getDouble(ObjectValue instance, String name) {
		return getDouble(instance, helper.<InstanceValue>checkNotNull(instance).getJavaClass(), name);
	}

	@Override
	public int getInt(ObjectValue instance, InstanceJavaClass klass, String name) {
		long offset = getFieldOffsetForInstance(instance, klass, name, "I");
		return instance.getData().readInt(offset);
	}

	@Override
	public int getInt(ObjectValue instance, String name) {
		return getInt(instance, helper.<InstanceValue>checkNotNull(instance).getJavaClass(), name);
	}

	@Override
	public float getFloat(ObjectValue instance, InstanceJavaClass klass, String name) {
		long offset = getFieldOffsetForInstance(instance, klass, name, "F");
		return Float.intBitsToFloat(instance.getData().readInt(offset));
	}

	@Override
	public float getFloat(ObjectValue instance, String name) {
		return getFloat(instance, helper.<InstanceValue>checkNotNull(instance).getJavaClass(), name);
	}

	@Override
	public char getChar(ObjectValue instance, InstanceJavaClass klass, String name) {
		long offset = getFieldOffsetForInstance(instance, klass, name, "C");
		return instance.getData().readChar(offset);
	}

	@Override
	public char getChar(ObjectValue instance, String name) {
		return getChar(instance, helper.<InstanceValue>checkNotNull(instance).getJavaClass(), name);
	}

	@Override
	public short getShort(ObjectValue instance, InstanceJavaClass klass, String name) {
		long offset = getFieldOffsetForInstance(instance, klass, name, "S");
		return instance.getData().readShort(offset);
	}

	@Override
	public short getShort(ObjectValue instance, String name) {
		return getShort(instance, helper.<InstanceValue>checkNotNull(instance).getJavaClass(), name);
	}

	@Override
	public byte getByte(ObjectValue instance, InstanceJavaClass klass, String name) {
		long offset = getFieldOffsetForInstance(instance, klass, name, "B");
		return instance.getData().readByte(offset);
	}

	@Override
	public byte getByte(ObjectValue instance, String name) {
		return getByte(instance, helper.<InstanceValue>checkNotNull(instance).getJavaClass(), name);
	}

	@Override
	public boolean getBoolean(ObjectValue instance, InstanceJavaClass klass, String name) {
		long offset = getFieldOffsetForInstance(instance, klass, name, "Z");
		return instance.getData().readByte(offset) != 0;
	}

	@Override
	public boolean getBoolean(ObjectValue instance, String name) {
		return getBoolean(instance, helper.<InstanceValue>checkNotNull(instance).getJavaClass(), name);
	}
	//</editor-fold>

	//<editor-fold desc="Static field get methods">
	@Override
	public ObjectValue getReference(InstanceJavaClass klass, String name, String desc) {
		JavaField field = linkResolver.resolveStaticField(klass, name, desc);
		klass = field.getOwner();
		MemoryManager memoryManager = this.memoryManager;
		return memoryManager.readReference(klass.getOop(), field.getOffset() + memoryManager.getStaticOffset(klass));
	}

	@Override
	public long getLong(InstanceJavaClass klass, String name) {
		JavaField field = linkResolver.resolveStaticField(klass, name, "J");
		klass = field.getOwner();
		return klass.getOop().getData().readLong(field.getOffset() + memoryManager.getStaticOffset(klass));
	}

	@Override
	public double getDouble(InstanceJavaClass klass, String name) {
		JavaField field = linkResolver.resolveStaticField(klass, name, "D");
		klass = field.getOwner();
		return Double.longBitsToDouble(klass.getOop().getData().readLong(field.getOffset() + memoryManager.getStaticOffset(klass)));
	}

	@Override
	public int getInt(InstanceJavaClass klass, String name) {
		JavaField field = linkResolver.resolveStaticField(klass, name, "I");
		klass = field.getOwner();
		return klass.getOop().getData().readInt(field.getOffset() + memoryManager.getStaticOffset(klass));
	}

	@Override
	public float getFloat(InstanceJavaClass klass, String name) {
		JavaField field = linkResolver.resolveStaticField(klass, name, "F");
		klass = field.getOwner();
		return Float.intBitsToFloat(klass.getOop().getData().readInt(field.getOffset() + memoryManager.getStaticOffset(klass)));
	}

	@Override
	public char getChar(InstanceJavaClass klass, String name) {
		JavaField field = linkResolver.resolveStaticField(klass, name, "C");
		klass = field.getOwner();
		return klass.getOop().getData().readChar(field.getOffset() + memoryManager.getStaticOffset(klass));
	}

	@Override
	public short getShort(InstanceJavaClass klass, String name) {
		JavaField field = linkResolver.resolveStaticField(klass, name, "S");
		klass = field.getOwner();
		return klass.getOop().getData().readShort(field.getOffset() + memoryManager.getStaticOffset(klass));
	}

	@Override
	public byte getByte(InstanceJavaClass klass, String name) {
		JavaField field = linkResolver.resolveStaticField(klass, name, "B");
		klass = field.getOwner();
		return klass.getOop().getData().readByte(field.getOffset() + memoryManager.getStaticOffset(klass));
	}

	@Override
	public boolean getBoolean(InstanceJavaClass klass, String name) {
		JavaField field = linkResolver.resolveStaticField(klass, name, "Z");
		klass = field.getOwner();
		return klass.getOop().getData().readByte(field.getOffset() + memoryManager.getStaticOffset(klass)) != 0;
	}
	//</editor-fold>

	//<editor-fold desc="Static field put methods">

	@Override
	public void putReference(InstanceJavaClass klass, String name, String desc, ObjectValue value) {
		JavaField field = linkResolver.resolveStaticField(klass, name, desc);
		klass = field.getOwner();
		MemoryManager memoryManager = this.memoryManager;
		memoryManager.writeValue(klass.getOop(), field.getOffset() + memoryManager.getStaticOffset(klass), value);
	}

	@Override
	public void putLong(InstanceJavaClass klass, String name, long value) {
		JavaField field = linkResolver.resolveStaticField(klass, name, "J");
		klass = field.getOwner();
		MemoryManager memoryManager = this.memoryManager;
		klass.getOop().getData().writeLong(field.getOffset() + memoryManager.getStaticOffset(klass), value);
	}

	@Override
	public void putDouble(InstanceJavaClass klass, String name, double value) {
		JavaField field = linkResolver.resolveStaticField(klass, name, "D");
		klass = field.getOwner();
		klass.getOop().getData().writeLong(field.getOffset() + memoryManager.getStaticOffset(klass), Double.doubleToRawLongBits(value));
	}

	@Override
	public void putInt(InstanceJavaClass klass, String name, int value) {
		JavaField field = linkResolver.resolveStaticField(klass, name, "I");
		klass = field.getOwner();
		klass.getOop().getData().writeInt(field.getOffset() + memoryManager.getStaticOffset(klass), value);
	}

	@Override
	public void putFloat(InstanceJavaClass klass, String name, float value) {
		JavaField field = linkResolver.resolveStaticField(klass, name, "F");
		klass = field.getOwner();
		klass.getOop().getData().writeInt(field.getOffset() + memoryManager.getStaticOffset(klass), Float.floatToRawIntBits(value));
	}

	@Override
	public void putChar(InstanceJavaClass klass, String name, char value) {
		JavaField field = linkResolver.resolveStaticField(klass, name, "C");
		klass = field.getOwner();
		klass.getOop().getData().writeChar(field.getOffset() + memoryManager.getStaticOffset(klass), value);
	}

	@Override
	public void putShort(InstanceJavaClass klass, String name, short value) {
		JavaField field = linkResolver.resolveStaticField(klass, name, "S");
		klass = field.getOwner();
		klass.getOop().getData().writeShort(field.getOffset() + memoryManager.getStaticOffset(klass), value);
	}

	@Override
	public void putByte(InstanceJavaClass klass, String name, byte value) {
		JavaField field = linkResolver.resolveStaticField(klass, name, "B");
		klass = field.getOwner();
		klass.getOop().getData().writeByte(field.getOffset() + memoryManager.getStaticOffset(klass), value);
	}

	@Override
	public void putBoolean(InstanceJavaClass klass, String name, boolean value) {
		JavaField field = linkResolver.resolveStaticField(klass, name, "Z");
		klass = field.getOwner();
		klass.getOop().getData().writeByte(field.getOffset() + memoryManager.getStaticOffset(klass), (byte) (value ? 1 : 0));
	}
	//</editor-fold>

	@Override
	public void monitorExit(ObjectValue value) {
		VMHelper helper = this.helper;
		if (!helper.<InstanceValue>checkNotNull(value).monitorExit()) {
			helper.throwException(symbols.java_lang_IllegalMonitorStateException());
		}
	}

	@Override
	public boolean instanceofCheck(ObjectValue value, JavaClass javaClass) {
		if (javaClass instanceof InstanceJavaClass) {
			((InstanceJavaClass) javaClass).loadNoResolve();
		}
		if (value.isNull()) {
			return false;
		}
		return javaClass.isAssignableFrom(value.getJavaClass());
	}

	private ArrayValue verifyArrayAccess(ObjectValue value, int index) {
		VMHelper helper = this.helper;
		ArrayValue array = helper.checkNotNullArray(value);
		helper.rangeCheck(array, index);
		return array;
	}

	private long getFieldOffsetForInstance(ObjectValue instance, InstanceJavaClass klass, String name, String desc) {
		VMHelper helper = this.helper;
		helper.checkNotNull(instance);
		JavaField field = linkResolver.resolveVirtualField(klass, (InstanceJavaClass) instance.getJavaClass(), name, desc);
		return field.getOffset() + memoryManager.valueBaseOffset(instance);
	}
}
