package dev.xdark.ssvm.util;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.memory.MemoryManager;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.symbol.VMSymbols;
import dev.xdark.ssvm.value.DoubleValue;
import dev.xdark.ssvm.value.FloatValue;
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

	public void putGenericField(ObjectValue instance, InstanceJavaClass klass, String name, String desc, Value value) {
		long offset = getFieldOffsetForInstance(instance, klass, name, desc);
		MemoryManager memoryManager = this.memoryManager;
		switch(desc.charAt(0)) {
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

	public void putField(ObjectValue instance, InstanceJavaClass klass, String name, String desc, Value value) {
		long offset = getFieldOffsetForInstance(instance, klass, name, desc);
		memoryManager.writeValue(instance, offset, (ObjectValue) value);
	}

	public void putLongField(ObjectValue instance, InstanceJavaClass klass, String name, String desc, long value) {
		long offset = getFieldOffsetForInstance(instance, klass, name, desc);
		memoryManager.writeLong(instance, offset, value);
	}

	public void putDoubleField(ObjectValue instance, InstanceJavaClass klass, String name, String desc, double value) {
		long offset = getFieldOffsetForInstance(instance, klass, name, desc);
		memoryManager.writeDouble(instance, offset, value);
	}

	public void putIntField(ObjectValue instance, InstanceJavaClass klass, String name, String desc, int value) {
		long offset = getFieldOffsetForInstance(instance, klass, name, desc);
		memoryManager.writeInt(instance, offset, value);
	}

	public void putFloatField(ObjectValue instance, InstanceJavaClass klass, String name, String desc, float value) {
		long offset = getFieldOffsetForInstance(instance, klass, name, desc);
		memoryManager.writeFloat(instance, offset, value);
	}

	public void putCharField(ObjectValue instance, InstanceJavaClass klass, String name, String desc, char value) {
		long offset = getFieldOffsetForInstance(instance, klass, name, desc);
		memoryManager.writeChar(instance, offset, value);
	}

	public void putShortField(ObjectValue instance, InstanceJavaClass klass, String name, String desc, short value) {
		long offset = getFieldOffsetForInstance(instance, klass, name, desc);
		memoryManager.writeShort(instance, offset, value);
	}

	public void putByteField(ObjectValue instance, InstanceJavaClass klass, String name, String desc, byte value) {
		long offset = getFieldOffsetForInstance(instance, klass, name, desc);
		memoryManager.writeByte(instance, offset, value);
	}

	public void putBooleanField(ObjectValue instance, InstanceJavaClass klass, String name, String desc, boolean value) {
		long offset = getFieldOffsetForInstance(instance, klass, name, desc);
		memoryManager.writeBoolean(instance, offset, value);
	}

	public Value getGenericField(ObjectValue instance, InstanceJavaClass klass, String name, String desc) {
		long offset = getFieldOffsetForInstance(instance, klass, name, desc);
		MemoryManager manager = this.memoryManager;
		Value value;
		switch(desc.charAt(0)) {
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

	public Value getField(ObjectValue instance, InstanceJavaClass klass, String name, String desc) {
		long offset = getFieldOffsetForInstance(instance, klass, name, desc);
		return memoryManager.readValue(instance, offset);
	}

	public long getLongField(ObjectValue instance, InstanceJavaClass klass, String name, String desc) {
		long offset = getFieldOffsetForInstance(instance, klass, name, desc);
		return memoryManager.readLong(instance, offset);
	}

	public double getDoubleField(ObjectValue instance, InstanceJavaClass klass, String name, String desc) {
		long offset = getFieldOffsetForInstance(instance, klass, name, desc);
		return memoryManager.readDouble(instance, offset);
	}

	public int getIntField(ObjectValue instance, InstanceJavaClass klass, String name, String desc) {
		long offset = getFieldOffsetForInstance(instance, klass, name, desc);
		return memoryManager.readInt(instance, offset);
	}

	public float getFloatField(ObjectValue instance, InstanceJavaClass klass, String name, String desc) {
		long offset = getFieldOffsetForInstance(instance, klass, name, desc);
		return memoryManager.readFloat(instance, offset);
	}

	public char getCharField(ObjectValue instance, InstanceJavaClass klass, String name, String desc) {
		long offset = getFieldOffsetForInstance(instance, klass, name, desc);
		return memoryManager.readChar(instance, offset);
	}

	public short getShortField(ObjectValue instance, InstanceJavaClass klass, String name, String desc) {
		long offset = getFieldOffsetForInstance(instance, klass, name, desc);
		return memoryManager.readShort(instance, offset);
	}

	public byte getByteField(ObjectValue instance, InstanceJavaClass klass, String name, String desc) {
		long offset = getFieldOffsetForInstance(instance, klass, name, desc);
		return memoryManager.readByte(instance, offset);
	}

	public boolean getBooleanField(ObjectValue instance, InstanceJavaClass klass, String name, String desc) {
		long offset = getFieldOffsetForInstance(instance, klass, name, desc);
		return memoryManager.readBoolean(instance, offset);
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
