package dev.xdark.ssvm.util;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.execution.VMException;
import dev.xdark.ssvm.memory.MemoryManager;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.mirror.JavaClass;
import dev.xdark.ssvm.symbol.VMSymbols;
import dev.xdark.ssvm.value.ArrayValue;
import dev.xdark.ssvm.value.DoubleValue;
import dev.xdark.ssvm.value.FloatValue;
import dev.xdark.ssvm.value.InstanceValue;
import dev.xdark.ssvm.value.IntValue;
import dev.xdark.ssvm.value.LongValue;
import dev.xdark.ssvm.value.ObjectValue;
import dev.xdark.ssvm.value.Value;

/**
 * Some VM operations implementations.
 *
 * @author xDArk
 */
public class VMOperations {

	private static final Value[] NO_VALUES = {};
	private final VMSymbols symbols;
	private final VMHelper helper;
	private final MemoryManager memoryManager;

	/**
	 * @param vm
	 * 		VM instance.
	 */
	public VMOperations(VirtualMachine vm) {
		symbols = vm.getSymbols();
		helper = vm.getHelper();
		memoryManager = vm.getMemoryManager();
	}

	/**
	 * Attempts to allocate new instance of a type of a class.
	 * Throws VM exception if allocation fails.
	 *
	 * @param klass
	 * 		Class to allocate an instance of.
	 *
	 * @return allocated instance.
	 *
	 * @see InstanceJavaClass#canAllocateInstance()
	 */
	public InstanceValue allocateInstance(InstanceJavaClass klass) {
		if (!klass.canAllocateInstance()) {
			helper.throwException(symbols.java_lang_InstantiationError());
		}
		return memoryManager.newInstance(klass);
	}

	/**
	 * Loads value from the array.
	 * Throws VM exception if array is null
	 * or index is out of bounds.
	 *
	 * @param array
	 * 		Array to get value from.
	 * @param index
	 * 		Value index.
	 *
	 * @return value from the array.
	 */
	public ObjectValue arrayLoadReference(ObjectValue array, int index) {
		return verifyArrayAccess(array, index).getValue(index);
	}

	/**
	 * Loads value from the array.
	 * Throws VM exception if array is null
	 * or index is out of bounds.
	 *
	 * @param array
	 * 		Array to get value from.
	 * @param index
	 * 		Value index.
	 *
	 * @return value from the array.
	 */
	public long arrayLoadLong(ObjectValue array, int index) {
		return verifyArrayAccess(array, index).getLong(index);
	}

	/**
	 * Loads value from the array.
	 * Throws VM exception if array is null
	 * or index is out of bounds.
	 *
	 * @param array
	 * 		Array to get value from.
	 * @param index
	 * 		Value index.
	 *
	 * @return value from the array.
	 */
	public double arrayLoadDouble(ObjectValue array, int index) {
		return verifyArrayAccess(array, index).getDouble(index);
	}

	/**
	 * Loads value from the array.
	 * Throws VM exception if array is null
	 * or index is out of bounds.
	 *
	 * @param array
	 * 		Array to get value from.
	 * @param index
	 * 		Value index.
	 *
	 * @return value from the array.
	 */
	public int arrayLoadInt(ObjectValue array, int index) {
		return verifyArrayAccess(array, index).getInt(index);
	}

	/**
	 * Loads value from the array.
	 * Throws VM exception if array is null
	 * or index is out of bounds.
	 *
	 * @param array
	 * 		Array to get value from.
	 * @param index
	 * 		Value index.
	 *
	 * @return value from the array.
	 */
	public float arrayLoadFloat(ObjectValue array, int index) {
		return verifyArrayAccess(array, index).getFloat(index);
	}

	/**
	 * Loads value from the array.
	 * Throws VM exception if array is null
	 * or index is out of bounds.
	 *
	 * @param array
	 * 		Array to get value from.
	 * @param index
	 * 		Value index.
	 *
	 * @return value from the array.
	 */
	public char arrayLoadChar(ObjectValue array, int index) {
		return verifyArrayAccess(array, index).getChar(index);
	}

	/**
	 * Loads value from the array.
	 * Throws VM exception if array is null
	 * or index is out of bounds.
	 *
	 * @param array
	 * 		Array to get value from.
	 * @param index
	 * 		Value index.
	 *
	 * @return value from the array.
	 */
	public short arrayLoadShort(ObjectValue array, int index) {
		return verifyArrayAccess(array, index).getShort(index);
	}

	/**
	 * Loads value from the array.
	 * Throws VM exception if array is null
	 * or index is out of bounds.
	 *
	 * @param array
	 * 		Array to get value from.
	 * @param index
	 * 		Value index.
	 *
	 * @return value from the array.
	 */
	public byte arrayLoadByte(ObjectValue array, int index) {
		return verifyArrayAccess(array, index).getByte(index);
	}

	/**
	 * Loads value from the array.
	 * Throws VM exception if array is null
	 * or index is out of bounds.
	 *
	 * @param array
	 * 		Array to get value from.
	 * @param index
	 * 		Value index.
	 *
	 * @return value from the array.
	 */
	public boolean arrayLoadBoolean(ObjectValue array, int index) {
		return verifyArrayAccess(array, index).getBoolean(index);
	}

	/**
	 * Sets value in the array.
	 * Throws VM exception if array is null
	 * or index is out of bounds.
	 *
	 * @param array
	 * 		Array to set value in.
	 * @param index
	 * 		Value index.
	 * @param value
	 * 		Value to set.
	 */
	public void arrayStoreReference(ObjectValue array, int index, ObjectValue value) {
		verifyArrayAccess(array, index).setValue(index, value);
	}

	/**
	 * Sets value in the array.
	 * Throws VM exception if array is null
	 * or index is out of bounds.
	 *
	 * @param array
	 * 		Array to set value in.
	 * @param index
	 * 		Value index.
	 * @param value
	 * 		Value to set.
	 */
	public void arrayStoreLong(ObjectValue array, int index, long value) {
		verifyArrayAccess(array, index).setLong(index, value);
	}

	/**
	 * Sets value in the array.
	 * Throws VM exception if array is null
	 * or index is out of bounds.
	 *
	 * @param array
	 * 		Array to set value in.
	 * @param index
	 * 		Value index.
	 * @param value
	 * 		Value to set.
	 */
	public void arrayStoreDouble(ObjectValue array, int index, double value) {
		verifyArrayAccess(array, index).setDouble(index, value);
	}

	/**
	 * Sets value in the array.
	 * Throws VM exception if array is null
	 * or index is out of bounds.
	 *
	 * @param array
	 * 		Array to set value in.
	 * @param index
	 * 		Value index.
	 * @param value
	 * 		Value to set.
	 */
	public void arrayStoreInt(ObjectValue array, int index, int value) {
		verifyArrayAccess(array, index).setInt(index, value);
	}

	/**
	 * Sets value in the array.
	 * Throws VM exception if array is null
	 * or index is out of bounds.
	 *
	 * @param array
	 * 		Array to set value in.
	 * @param index
	 * 		Value index.
	 * @param value
	 * 		Value to set.
	 */
	public void arrayStoreFloat(ObjectValue array, int index, float value) {
		verifyArrayAccess(array, index).setFloat(index, value);
	}

	/**
	 * Sets value in the array.
	 * Throws VM exception if array is null
	 * or index is out of bounds.
	 *
	 * @param array
	 * 		Array to set value in.
	 * @param index
	 * 		Value index.
	 * @param value
	 * 		Value to set.
	 */
	public void arrayStoreChar(ObjectValue array, int index, char value) {
		verifyArrayAccess(array, index).setChar(index, value);
	}

	/**
	 * Sets value in the array.
	 * Throws VM exception if array is null
	 * or index is out of bounds.
	 *
	 * @param array
	 * 		Array to set value in.
	 * @param index
	 * 		Value index.
	 * @param value
	 * 		Value to set.
	 */
	public void arrayStoreShort(ObjectValue array, int index, short value) {
		verifyArrayAccess(array, index).setShort(index, value);
	}

	/**
	 * Sets value in the array.
	 * Throws VM exception if array is null
	 * or index is out of bounds.
	 *
	 * @param array
	 * 		Array to set value in.
	 * @param index
	 * 		Value index.
	 * @param value
	 * 		Value to set.
	 */
	public void arrayStoreByte(ObjectValue array, int index, byte value) {
		verifyArrayAccess(array, index).setByte(index, value);
	}

	/**
	 * Sets value in the array.
	 * Throws VM exception if array is null
	 * or index is out of bounds.
	 *
	 * @param array
	 * 		Array to set value in.
	 * @param index
	 * 		Value index.
	 * @param value
	 * 		Value to set.
	 */
	public void arrayStoreBoolean(ObjectValue array, int index, boolean value) {
		verifyArrayAccess(array, index).setBoolean(index, value);
	}

	/**
	 * Casts an object, throws
	 * VM exception if cast failed.
	 *
	 * @param value
	 * 		Value to attempt to cast.
	 * @param klass
	 * 		Class to cast value to.
	 *
	 * @return Same value.
	 */
	public ObjectValue checkCast(ObjectValue value, JavaClass klass) {
		if (!value.isNull()) {
			JavaClass against = value.getJavaClass();
			if (!klass.isAssignableFrom(against)) {
				helper.throwException(symbols.java_lang_ClassCastException(), against.getName() + " cannot be cast to " + klass.getName());
			}
		}
		return value;
	}

	/**
	 * Throws VM exception.
	 *
	 * @param value
	 * 		Exception to throw.
	 */
	public void throwException(ObjectValue value) {
		if (value.isNull()) {
			// NPE it is then.
			InstanceJavaClass exceptionClass = symbols.java_lang_NullPointerException();
			exceptionClass.initialize();
			value = memoryManager.newInstance(exceptionClass);
			helper.invokeExact(exceptionClass, "<init>", "()V", NO_VALUES, new Value[]{value});
		}
		throw new VMException((InstanceValue) value);
	}

	/**
	 * Sets generic value into an instance.
	 * Throws VM exception if field was not found,
	 * or an instance is {@code null}.
	 *
	 * @param instance
	 * 		Instance to set value in.
	 * @param klass
	 * 		Field base class.
	 * @param name
	 * 		Field name.
	 * @param desc
	 * 		Field descriptor.
	 * @param value
	 * 		Value to set.
	 */
	public void putGenericField(ObjectValue instance, InstanceJavaClass klass, String name, String desc, Value value) {
		long offset = getFieldOffsetForInstance(instance, klass, name, desc);
		writeGenericValue(instance, desc, value, offset);
	}

	/**
	 * Sets reference value in an instance.
	 * Throws VM exception if field was not found,
	 * or an instance is {@code null}.
	 *
	 * @param instance
	 * 		Instance to set value in.
	 * @param klass
	 * 		Field base class.
	 * @param name
	 * 		Field name.
	 * @param desc
	 * 		Field descriptor.
	 * @param value
	 * 		Value to set.
	 */
	public void putField(ObjectValue instance, InstanceJavaClass klass, String name, String desc, ObjectValue value) {
		long offset = getFieldOffsetForInstance(instance, klass, name, desc);
		ReferenceCountUtil.tryRetain(value);
		ObjectValue oldValue = memoryManager.getAndWriteValue(instance, offset, value);
		ReferenceCountUtil.tryRelease(oldValue);
	}

	/**
	 * Sets long value in an instance.
	 * Throws VM exception if field was not found,
	 * or an instance is {@code null}.
	 *
	 * @param instance
	 * 		Instance to set value in.
	 * @param klass
	 * 		Field base class.
	 * @param name
	 * 		Field name.
	 * @param value
	 * 		Value to set.
	 */
	public void putLongField(ObjectValue instance, InstanceJavaClass klass, String name, long value) {
		long offset = getFieldOffsetForInstance(instance, klass, name, "J");
		memoryManager.writeLong(instance, offset, value);
	}

	/**
	 * Sets double value in an instance.
	 * Throws VM exception if field was not found,
	 * or an instance is {@code null}.
	 *
	 * @param instance
	 * 		Instance to set value in.
	 * @param klass
	 * 		Field base class.
	 * @param name
	 * 		Field name.
	 * @param value
	 * 		Value to set.
	 */
	public void putDoubleField(ObjectValue instance, InstanceJavaClass klass, String name, double value) {
		long offset = getFieldOffsetForInstance(instance, klass, name, "D");
		memoryManager.writeDouble(instance, offset, value);
	}

	/**
	 * Sets int value in an instance.
	 * Throws VM exception if field was not found,
	 * or an instance is {@code null}.
	 *
	 * @param instance
	 * 		Instance to set value in.
	 * @param klass
	 * 		Field base class.
	 * @param name
	 * 		Field name.
	 * @param value
	 * 		Value to set.
	 */
	public void putIntField(ObjectValue instance, InstanceJavaClass klass, String name, int value) {
		long offset = getFieldOffsetForInstance(instance, klass, name, "I");
		memoryManager.writeInt(instance, offset, value);
	}

	/**
	 * Sets float value in an instance.
	 * Throws VM exception if field was not found,
	 * or an instance is {@code null}.
	 *
	 * @param instance
	 * 		Instance to set value in.
	 * @param klass
	 * 		Field base class.
	 * @param name
	 * 		Field name.
	 * @param value
	 * 		Value to set.
	 */
	public void putFloatField(ObjectValue instance, InstanceJavaClass klass, String name, float value) {
		long offset = getFieldOffsetForInstance(instance, klass, name, "F");
		memoryManager.writeFloat(instance, offset, value);
	}

	/**
	 * Sets char value in an instance.
	 * Throws VM exception if field was not found,
	 * or an instance is {@code null}.
	 *
	 * @param instance
	 * 		Instance to set value in.
	 * @param klass
	 * 		Field base class.
	 * @param name
	 * 		Field name.
	 * @param value
	 * 		Value to set.
	 */
	public void putCharField(ObjectValue instance, InstanceJavaClass klass, String name, char value) {
		long offset = getFieldOffsetForInstance(instance, klass, name, "C");
		memoryManager.writeChar(instance, offset, value);
	}

	/**
	 * Sets short value in an instance.
	 * Throws VM exception if field was not found,
	 * or an instance is {@code null}.
	 *
	 * @param instance
	 * 		Instance to set value in.
	 * @param klass
	 * 		Field base class.
	 * @param name
	 * 		Field name.
	 * @param value
	 * 		Value to set.
	 */
	public void putShortField(ObjectValue instance, InstanceJavaClass klass, String name, short value) {
		long offset = getFieldOffsetForInstance(instance, klass, name, "S");
		memoryManager.writeShort(instance, offset, value);
	}

	/**
	 * Sets byte value in an instance.
	 * Throws VM exception if field was not found,
	 * or an instance is {@code null}.
	 *
	 * @param instance
	 * 		Instance to set value in.
	 * @param klass
	 * 		Field base class.
	 * @param name
	 * 		Field name.
	 * @param value
	 * 		Value to set.
	 */
	public void putByteField(ObjectValue instance, InstanceJavaClass klass, String name, byte value) {
		long offset = getFieldOffsetForInstance(instance, klass, name, "B");
		memoryManager.writeByte(instance, offset, value);
	}

	/**
	 * Sets boolean value in an instance.
	 * Throws VM exception if field was not found,
	 * or an instance is {@code null}.
	 *
	 * @param instance
	 * 		Instance to set value in.
	 * @param klass
	 * 		Field base class.
	 * @param name
	 * 		Field name.
	 * @param value
	 * 		Value to set.
	 */
	public void putBooleanField(ObjectValue instance, InstanceJavaClass klass, String name, boolean value) {
		long offset = getFieldOffsetForInstance(instance, klass, name, "Z");
		memoryManager.writeBoolean(instance, offset, value);
	}

	/**
	 * Gets generic value from an instance.
	 * Throws VM exception if field was not found,
	 * or an instance is {@code null}.
	 *
	 * @param instance
	 * 		Instance to get value from.
	 * @param klass
	 * 		Field base class.
	 * @param name
	 * 		Field name.
	 * @param desc
	 * 		Field desc.
	 *
	 * @return field value.
	 */
	public Value getGenericField(ObjectValue instance, InstanceJavaClass klass, String name, String desc) {
		long offset = getFieldOffsetForInstance(instance, klass, name, desc);
		return readGenericValue((InstanceValue) instance, desc, offset);
	}

	/**
	 * Gets reference value from an instance.
	 * Throws VM exception if field was not found,
	 * or an instance is {@code null}.
	 *
	 * @param instance
	 * 		Instance to get value from.
	 * @param klass
	 * 		Field base class.
	 * @param name
	 * 		Field name.
	 * @param desc
	 * 		Field desc.
	 *
	 * @return field value.
	 */
	public ObjectValue getField(ObjectValue instance, InstanceJavaClass klass, String name, String desc) {
		long offset = getFieldOffsetForInstance(instance, klass, name, desc);
		return memoryManager.readValue(instance, offset);
	}

	/**
	 * Gets long value from an instance.
	 * Throws VM exception if field was not found,
	 * or an instance is {@code null}.
	 *
	 * @param instance
	 * 		Instance to get value from.
	 * @param klass
	 * 		Field base class.
	 * @param name
	 * 		Field name.
	 *
	 * @return field value.
	 */
	public long getLongField(ObjectValue instance, InstanceJavaClass klass, String name) {
		long offset = getFieldOffsetForInstance(instance, klass, name, "J");
		return memoryManager.readLong(instance, offset);
	}

	/**
	 * Gets double value from an instance.
	 * Throws VM exception if field was not found,
	 * or an instance is {@code null}.
	 *
	 * @param instance
	 * 		Instance to get value from.
	 * @param klass
	 * 		Field base class.
	 * @param name
	 * 		Field name.
	 *
	 * @return field value.
	 */
	public double getDoubleField(ObjectValue instance, InstanceJavaClass klass, String name) {
		long offset = getFieldOffsetForInstance(instance, klass, name, "D");
		return memoryManager.readDouble(instance, offset);
	}

	/**
	 * Gets int value from an instance.
	 * Throws VM exception if field was not found,
	 * or an instance is {@code null}.
	 *
	 * @param instance
	 * 		Instance to get value from.
	 * @param klass
	 * 		Field base class.
	 * @param name
	 * 		Field name.
	 *
	 * @return field value.
	 */
	public int getIntField(ObjectValue instance, InstanceJavaClass klass, String name) {
		long offset = getFieldOffsetForInstance(instance, klass, name, "I");
		return memoryManager.readInt(instance, offset);
	}

	/**
	 * Gets float value from an instance.
	 * Throws VM exception if field was not found,
	 * or an instance is {@code null}.
	 *
	 * @param instance
	 * 		Instance to get value from.
	 * @param klass
	 * 		Field base class.
	 * @param name
	 * 		Field name.
	 *
	 * @return field value.
	 */
	public float getFloatField(ObjectValue instance, InstanceJavaClass klass, String name) {
		long offset = getFieldOffsetForInstance(instance, klass, name, "F");
		return memoryManager.readFloat(instance, offset);
	}

	/**
	 * Gets char value from an instance.
	 * Throws VM exception if field was not found,
	 * or an instance is {@code null}.
	 *
	 * @param instance
	 * 		Instance to get value from.
	 * @param klass
	 * 		Field base class.
	 * @param name
	 * 		Field name.
	 *
	 * @return field value.
	 */
	public char getCharField(ObjectValue instance, InstanceJavaClass klass, String name) {
		long offset = getFieldOffsetForInstance(instance, klass, name, "C");
		return memoryManager.readChar(instance, offset);
	}

	/**
	 * Gets short value from an instance.
	 * Throws VM exception if field was not found,
	 * or an instance is {@code null}.
	 *
	 * @param instance
	 * 		Instance to get value from.
	 * @param klass
	 * 		Field base class.
	 * @param name
	 * 		Field name.
	 *
	 * @return field value.
	 */
	public short getShortField(ObjectValue instance, InstanceJavaClass klass, String name) {
		long offset = getFieldOffsetForInstance(instance, klass, name, "S");
		return memoryManager.readShort(instance, offset);
	}

	/**
	 * Gets byte value from an instance.
	 * Throws VM exception if field was not found,
	 * or an instance is {@code null}.
	 *
	 * @param instance
	 * 		Instance to get value from.
	 * @param klass
	 * 		Field base class.
	 * @param name
	 * 		Field name.
	 *
	 * @return field value.
	 */
	public byte getByteField(ObjectValue instance, InstanceJavaClass klass, String name) {
		long offset = getFieldOffsetForInstance(instance, klass, name, "B");
		return memoryManager.readByte(instance, offset);
	}

	/**
	 * Gets boolean value from an instance.
	 * Throws VM exception if field was not found,
	 * or an instance is {@code null}.
	 *
	 * @param instance
	 * 		Instance to get value from.
	 * @param klass
	 * 		Field base class.
	 * @param name
	 * 		Field name.
	 *
	 * @return field value.
	 */
	public boolean getBooleanField(ObjectValue instance, InstanceJavaClass klass, String name) {
		long offset = getFieldOffsetForInstance(instance, klass, name, "Z");
		return memoryManager.readBoolean(instance, offset);
	}

	/**
	 * Gets static generic value from a class.
	 * Throws VM exception if field was not found.
	 *
	 * @param klass
	 * 		Class to get value from.
	 * @param name
	 * 		Field name.
	 * @param desc
	 * 		Field descriptor.
	 *
	 * @return field value.
	 */
	public Value getGenericStaticField(InstanceJavaClass klass, String name, String desc) {
		while (klass != null) {
			long offset = klass.getStaticFieldOffset(name, desc);
			if (offset != -1L) {
				return readGenericValue(klass.getOop(), desc, offset + memoryManager.getStaticOffset(klass));
			}
			klass = klass.getSuperClass();
		}
		helper.throwException(symbols.java_lang_NoSuchFieldError(), name);
		return null;
	}

	/**
	 * Gets static reference value from a class.
	 * Throws VM exception if field was not found.
	 *
	 * @param klass
	 * 		Class to get value from.
	 * @param name
	 * 		Field name.
	 * @param desc
	 * 		Field descriptor.
	 *
	 * @return field value.
	 */
	public Value getStaticField(InstanceJavaClass klass, String name, String desc) {
		while (klass != null) {
			long offset = klass.getStaticFieldOffset(name, desc);
			if (offset != -1L) {
				MemoryManager memoryManager = this.memoryManager;
				return memoryManager.readValue(klass.getOop(), offset + memoryManager.getStaticOffset(klass));
			}
			klass = klass.getSuperClass();
		}
		helper.throwException(symbols.java_lang_NoSuchFieldError(), name);
		return null;
	}

	/**
	 * Gets static long value from a class.
	 * Throws VM exception if field was not found.
	 *
	 * @param klass
	 * 		Class to get value from.
	 * @param name
	 * 		Field name.
	 *
	 * @return field value.
	 */
	public long getStaticLongField(InstanceJavaClass klass, String name) {
		while (klass != null) {
			long offset = klass.getStaticFieldOffset(name, "J");
			if (offset != -1L) {
				MemoryManager memoryManager = this.memoryManager;
				return memoryManager.readLong(klass.getOop(), offset + memoryManager.getStaticOffset(klass));
			}
			klass = klass.getSuperClass();
		}
		helper.throwException(symbols.java_lang_NoSuchFieldError(), name);
		return 0L;
	}

	/**
	 * Gets static double value from a class.
	 * Throws VM exception if field was not found.
	 *
	 * @param klass
	 * 		Class to get value from.
	 * @param name
	 * 		Field name.
	 *
	 * @return field value.
	 */
	public double getStaticDoubleField(InstanceJavaClass klass, String name) {
		while (klass != null) {
			long offset = klass.getStaticFieldOffset(name, "D");
			if (offset != -1L) {
				MemoryManager memoryManager = this.memoryManager;
				return memoryManager.readDouble(klass.getOop(), offset + memoryManager.getStaticOffset(klass));
			}
			klass = klass.getSuperClass();
		}
		helper.throwException(symbols.java_lang_NoSuchFieldError(), name);
		return Double.NaN;
	}

	/**
	 * Gets static int value from a class.
	 * Throws VM exception if field was not found.
	 *
	 * @param klass
	 * 		Class to get value from.
	 * @param name
	 * 		Field name.
	 *
	 * @return field value.
	 */
	public int getStaticIntField(InstanceJavaClass klass, String name) {
		while (klass != null) {
			long offset = klass.getStaticFieldOffset(name, "I");
			if (offset != -1L) {
				MemoryManager memoryManager = this.memoryManager;
				return memoryManager.readInt(klass.getOop(), offset + memoryManager.getStaticOffset(klass));
			}
			klass = klass.getSuperClass();
		}
		helper.throwException(symbols.java_lang_NoSuchFieldError(), name);
		return 0;
	}

	/**
	 * Gets static float value from a class.
	 * Throws VM exception if field was not found.
	 *
	 * @param klass
	 * 		Class to get value from.
	 * @param name
	 * 		Field name.
	 *
	 * @return field value.
	 */
	public float getStaticFloatField(InstanceJavaClass klass, String name) {
		while (klass != null) {
			long offset = klass.getStaticFieldOffset(name, "F");
			if (offset != -1L) {
				MemoryManager memoryManager = this.memoryManager;
				return memoryManager.readFloat(klass.getOop(), offset + memoryManager.getStaticOffset(klass));
			}
			klass = klass.getSuperClass();
		}
		helper.throwException(symbols.java_lang_NoSuchFieldError(), name);
		return 0.0F;
	}

	/**
	 * Gets static char value from a class.
	 * Throws VM exception if field was not found.
	 *
	 * @param klass
	 * 		Class to get value from.
	 * @param name
	 * 		Field name.
	 *
	 * @return field value.
	 */
	public char getStaticCharField(InstanceJavaClass klass, String name) {
		while (klass != null) {
			long offset = klass.getStaticFieldOffset(name, "C");
			if (offset != -1L) {
				MemoryManager memoryManager = this.memoryManager;
				return memoryManager.readChar(klass.getOop(), offset + memoryManager.getStaticOffset(klass));
			}
			klass = klass.getSuperClass();
		}
		helper.throwException(symbols.java_lang_NoSuchFieldError(), name);
		return '\0';
	}

	/**
	 * Gets static short value from a class.
	 * Throws VM exception if field was not found.
	 *
	 * @param klass
	 * 		Class to get value from.
	 * @param name
	 * 		Field name.
	 *
	 * @return field value.
	 */
	public short getStaticShortField(InstanceJavaClass klass, String name) {
		while (klass != null) {
			long offset = klass.getStaticFieldOffset(name, "S");
			if (offset != -1L) {
				MemoryManager memoryManager = this.memoryManager;
				return memoryManager.readShort(klass.getOop(), offset + memoryManager.getStaticOffset(klass));
			}
			klass = klass.getSuperClass();
		}
		helper.throwException(symbols.java_lang_NoSuchFieldError(), name);
		return 0;
	}

	/**
	 * Gets static byte value from a class.
	 * Throws VM exception if field was not found.
	 *
	 * @param klass
	 * 		Class to get value from.
	 * @param name
	 * 		Field name.
	 *
	 * @return field value.
	 */
	public byte getStaticByteField(InstanceJavaClass klass, String name) {
		while (klass != null) {
			long offset = klass.getStaticFieldOffset(name, "B");
			if (offset != -1L) {
				MemoryManager memoryManager = this.memoryManager;
				return memoryManager.readByte(klass.getOop(), offset + memoryManager.getStaticOffset(klass));
			}
			klass = klass.getSuperClass();
		}
		helper.throwException(symbols.java_lang_NoSuchFieldError(), name);
		return 0;
	}

	/**
	 * Gets static boolean value from a class.
	 * Throws VM exception if field was not found.
	 *
	 * @param klass
	 * 		Class to get value from.
	 * @param name
	 * 		Field name.
	 *
	 * @return field value.
	 */
	public boolean getStaticBooleanField(InstanceJavaClass klass, String name) {
		while (klass != null) {
			long offset = klass.getStaticFieldOffset(name, "Z");
			if (offset != -1L) {
				MemoryManager memoryManager = this.memoryManager;
				return memoryManager.readBoolean(klass.getOop(), offset + memoryManager.getStaticOffset(klass));
			}
			klass = klass.getSuperClass();
		}
		helper.throwException(symbols.java_lang_NoSuchFieldError(), name);
		return false;
	}

	/**
	 * Sets static generic value in a class.
	 * Throws VM exception if field was not found.
	 *
	 * @param klass
	 * 		Class to set value for.
	 * @param name
	 * 		Field name.
	 * @param desc
	 * 		Field descriptor.
	 * @param value
	 * 		Value to set.
	 */
	public void putStaticGenericField(InstanceJavaClass klass, String name, String desc, Value value) {
		while (klass != null) {
			long offset = klass.getStaticFieldOffset(name, desc);
			if (offset != -1L) {
				MemoryManager memoryManager = this.memoryManager;
				writeGenericValue(klass.getOop(), desc, value, offset + memoryManager.getStaticOffset(klass));
				return;
			}
			klass = klass.getSuperClass();
		}
		helper.throwException(symbols.java_lang_NoSuchFieldError(), name);
	}

	/**
	 * Sets static reference value in a class.
	 * Throws VM exception if field was not found.
	 *
	 * @param klass
	 * 		Class to set value for.
	 * @param name
	 * 		Field name.
	 * @param desc
	 * 		Field descriptor.
	 * @param value
	 * 		Value to set.
	 */
	public void putStaticField(InstanceJavaClass klass, String name, String desc, ObjectValue value) {
		while (klass != null) {
			long offset = klass.getStaticFieldOffset(name, desc);
			if (offset != -1L) {
				MemoryManager memoryManager = this.memoryManager;
				memoryManager.writeValue(klass.getOop(), offset + memoryManager.getStaticOffset(klass), value);
				return;
			}
			klass = klass.getSuperClass();
		}
		helper.throwException(symbols.java_lang_NoSuchFieldError(), name);
	}

	/**
	 * Sets static long value in a class.
	 * Throws VM exception if field was not found.
	 *
	 * @param klass
	 * 		Class to set value for.
	 * @param name
	 * 		Field name.
	 * @param value
	 * 		Value to set.
	 */
	public void putStaticLongField(InstanceJavaClass klass, String name, long value) {
		while (klass != null) {
			long offset = klass.getStaticFieldOffset(name, "J");
			if (offset != -1L) {
				MemoryManager memoryManager = this.memoryManager;
				memoryManager.writeLong(klass.getOop(), offset + memoryManager.getStaticOffset(klass), value);
				return;
			}
			klass = klass.getSuperClass();
		}
		helper.throwException(symbols.java_lang_NoSuchFieldError(), name);
	}

	/**
	 * Sets static double value in a class.
	 * Throws VM exception if field was not found.
	 *
	 * @param klass
	 * 		Class to set value for.
	 * @param name
	 * 		Field name.
	 * @param value
	 * 		Value to set.
	 */
	public void putStaticDoubleField(InstanceJavaClass klass, String name, double value) {
		while (klass != null) {
			long offset = klass.getStaticFieldOffset(name, "D");
			if (offset != -1L) {
				MemoryManager memoryManager = this.memoryManager;
				memoryManager.writeDouble(klass.getOop(), offset + memoryManager.getStaticOffset(klass), value);
				return;
			}
			klass = klass.getSuperClass();
		}
		helper.throwException(symbols.java_lang_NoSuchFieldError(), name);
	}

	/**
	 * Sets static int value in a class.
	 * Throws VM exception if field was not found.
	 *
	 * @param klass
	 * 		Class to set value for.
	 * @param name
	 * 		Field name.
	 * @param value
	 * 		Value to set.
	 */
	public void putStaticIntField(InstanceJavaClass klass, String name, int value) {
		while (klass != null) {
			long offset = klass.getStaticFieldOffset(name, "I");
			if (offset != -1L) {
				MemoryManager memoryManager = this.memoryManager;
				memoryManager.writeInt(klass.getOop(), offset + memoryManager.getStaticOffset(klass), value);
				return;
			}
			klass = klass.getSuperClass();
		}
		helper.throwException(symbols.java_lang_NoSuchFieldError(), name);
	}

	/**
	 * Sets static float value in a class.
	 * Throws VM exception if field was not found.
	 *
	 * @param klass
	 * 		Class to set value for.
	 * @param name
	 * 		Field name.
	 * @param value
	 * 		Value to set.
	 */
	public void putStaticFloatField(InstanceJavaClass klass, String name, float value) {
		while (klass != null) {
			long offset = klass.getStaticFieldOffset(name, "F");
			if (offset != -1L) {
				MemoryManager memoryManager = this.memoryManager;
				memoryManager.writeFloat(klass.getOop(), offset + memoryManager.getStaticOffset(klass), value);
				return;
			}
			klass = klass.getSuperClass();
		}
		helper.throwException(symbols.java_lang_NoSuchFieldError(), name);
	}

	/**
	 * Sets static char value in a class.
	 * Throws VM exception if field was not found.
	 *
	 * @param klass
	 * 		Class to set value for.
	 * @param name
	 * 		Field name.
	 * @param value
	 * 		Value to set.
	 */
	public void putStaticCharField(InstanceJavaClass klass, String name, char value) {
		while (klass != null) {
			long offset = klass.getStaticFieldOffset(name, "C");
			if (offset != -1L) {
				MemoryManager memoryManager = this.memoryManager;
				memoryManager.writeChar(klass.getOop(), offset + memoryManager.getStaticOffset(klass), value);
				return;
			}
			klass = klass.getSuperClass();
		}
		helper.throwException(symbols.java_lang_NoSuchFieldError(), name);
	}

	/**
	 * Sets static short value in a class.
	 * Throws VM exception if field was not found.
	 *
	 * @param klass
	 * 		Class to set value for.
	 * @param name
	 * 		Field name.
	 * @param value
	 * 		Value to set.
	 */
	public void putStaticShortField(InstanceJavaClass klass, String name, short value) {
		while (klass != null) {
			long offset = klass.getStaticFieldOffset(name, "S");
			if (offset != -1L) {
				MemoryManager memoryManager = this.memoryManager;
				memoryManager.writeShort(klass.getOop(), offset + memoryManager.getStaticOffset(klass), value);
				return;
			}
			klass = klass.getSuperClass();
		}
		helper.throwException(symbols.java_lang_NoSuchFieldError(), name);
	}

	/**
	 * Sets static byte value in a class.
	 * Throws VM exception if field was not found.
	 *
	 * @param klass
	 * 		Class to set value for.
	 * @param name
	 * 		Field name.
	 * @param value
	 * 		Value to set.
	 */
	public void putStaticByteField(InstanceJavaClass klass, String name, byte value) {
		while (klass != null) {
			long offset = klass.getStaticFieldOffset(name, "B");
			if (offset != -1L) {
				MemoryManager memoryManager = this.memoryManager;
				memoryManager.writeByte(klass.getOop(), offset + memoryManager.getStaticOffset(klass), value);
				return;
			}
			klass = klass.getSuperClass();
		}
		helper.throwException(symbols.java_lang_NoSuchFieldError(), name);
	}

	/**
	 * Sets static boolean value in a class.
	 * Throws VM exception if field was not found.
	 *
	 * @param klass
	 * 		Class to set value for.
	 * @param name
	 * 		Field name.
	 * @param value
	 * 		Value to set.
	 */
	public void putStaticBooleanField(InstanceJavaClass klass, String name, boolean value) {
		while (klass != null) {
			long offset = klass.getStaticFieldOffset(name, "Z");
			if (offset != -1L) {
				MemoryManager memoryManager = this.memoryManager;
				memoryManager.writeBoolean(klass.getOop(), offset + memoryManager.getStaticOffset(klass), value);
				return;
			}
			klass = klass.getSuperClass();
		}
		helper.throwException(symbols.java_lang_NoSuchFieldError(), name);
	}

	/**
	 * Reads generic value from the instance.
	 *
	 * @param instance
	 * 		Instance to read value from.
	 * @param desc
	 * 		Type descriptor.
	 * @param offset
	 * 		Offset to read value from.
	 */
	public Value readGenericValue(InstanceValue instance, String desc, long offset) {
		MemoryManager manager = this.memoryManager;
		Value value;
		switch (desc.charAt(0)) {
			case 'J':
				value = LongValue.of(manager.readLong(instance, offset));
				break;
			case 'D':
				value = new DoubleValue(manager.readDouble(instance, offset));
				break;
			case 'I':
				value = IntValue.of(manager.readInt(instance, offset));
				break;
			case 'F':
				value = new FloatValue(manager.readFloat(instance, offset));
				break;
			case 'C':
				value = IntValue.of(manager.readChar(instance, offset));
				break;
			case 'S':
				value = IntValue.of(manager.readShort(instance, offset));
				break;
			case 'B':
				value = IntValue.of(manager.readByte(instance, offset));
				break;
			case 'Z':
				value = manager.readBoolean(instance, offset) ? IntValue.ONE : IntValue.ZERO;
				break;
			default:
				value = manager.readValue(instance, offset);
		}
		return value;
	}

	/**
	 * Writes generic value to the instance.
	 *
	 * @param instance
	 * 		Instance to write value to.
	 * @param desc
	 * 		Type descriptor.
	 * @param value
	 * 		Value to write.
	 * @param offset
	 * 		Offset to write value to.
	 */
	public void writeGenericValue(ObjectValue instance, String desc, Value value, long offset) {
		MemoryManager memoryManager = this.memoryManager;
		switch (desc.charAt(0)) {
			case 'J':
				memoryManager.writeLong(instance, offset, value.asLong());
				break;
			case 'D':
				memoryManager.writeDouble(instance, offset, value.asDouble());
				break;
			case 'I':
				memoryManager.writeInt(instance, offset, value.asInt());
				break;
			case 'F':
				memoryManager.writeFloat(instance, offset, value.asFloat());
				break;
			case 'C':
				memoryManager.writeChar(instance, offset, value.asChar());
				break;
			case 'S':
				memoryManager.writeShort(instance, offset, value.asShort());
				break;
			case 'B':
				memoryManager.writeByte(instance, offset, value.asByte());
				break;
			case 'Z':
				memoryManager.writeBoolean(instance, offset, value.asBoolean());
				break;
			default:
				memoryManager.writeValue(instance, offset, (ObjectValue) value);
		}
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
		long offset = helper.getFieldOffset(klass, (InstanceJavaClass) instance.getJavaClass(), name, desc);
		if (offset == -1L) {
			helper.throwException(symbols.java_lang_NoSuchFieldError(), name);
		}
		return offset + memoryManager.valueBaseOffset(instance);
	}
}
