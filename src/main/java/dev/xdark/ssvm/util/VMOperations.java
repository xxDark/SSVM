package dev.xdark.ssvm.util;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.memory.MemoryManager;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.symbol.VMSymbols;
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
		writeGenericValue(instance, desc, value, offset);
	}

	public void putField(ObjectValue instance, InstanceJavaClass klass, String name, String desc, ObjectValue value) {
		long offset = getFieldOffsetForInstance(instance, klass, name, desc);
		memoryManager.writeValue(instance, offset, value);
	}

	public void putLongField(ObjectValue instance, InstanceJavaClass klass, String name, long value) {
		long offset = getFieldOffsetForInstance(instance, klass, name, "J");
		memoryManager.writeLong(instance, offset, value);
	}

	public void putDoubleField(ObjectValue instance, InstanceJavaClass klass, String name, double value) {
		long offset = getFieldOffsetForInstance(instance, klass, name, "D");
		memoryManager.writeDouble(instance, offset, value);
	}

	public void putIntField(ObjectValue instance, InstanceJavaClass klass, String name, int value) {
		long offset = getFieldOffsetForInstance(instance, klass, name, "I");
		memoryManager.writeInt(instance, offset, value);
	}

	public void putFloatField(ObjectValue instance, InstanceJavaClass klass, String name, float value) {
		long offset = getFieldOffsetForInstance(instance, klass, name, "F");
		memoryManager.writeFloat(instance, offset, value);
	}

	public void putCharField(ObjectValue instance, InstanceJavaClass klass, String name, char value) {
		long offset = getFieldOffsetForInstance(instance, klass, name, "C");
		memoryManager.writeChar(instance, offset, value);
	}

	public void putShortField(ObjectValue instance, InstanceJavaClass klass, String name, short value) {
		long offset = getFieldOffsetForInstance(instance, klass, name, "S");
		memoryManager.writeShort(instance, offset, value);
	}

	public void putByteField(ObjectValue instance, InstanceJavaClass klass, String name, byte value) {
		long offset = getFieldOffsetForInstance(instance, klass, name, "B");
		memoryManager.writeByte(instance, offset, value);
	}

	public void putBooleanField(ObjectValue instance, InstanceJavaClass klass, String name, boolean value) {
		long offset = getFieldOffsetForInstance(instance, klass, name, "Z");
		memoryManager.writeBoolean(instance, offset, value);
	}

	public Value getGenericField(ObjectValue instance, InstanceJavaClass klass, String name, String desc) {
		long offset = getFieldOffsetForInstance(instance, klass, name, desc);
		return readGenericValue(desc, offset, (InstanceValue) instance);
	}

	public Value getField(ObjectValue instance, InstanceJavaClass klass, String name, String desc) {
		long offset = getFieldOffsetForInstance(instance, klass, name, desc);
		return memoryManager.readValue(instance, offset);
	}

	public long getLongField(ObjectValue instance, InstanceJavaClass klass, String name) {
		long offset = getFieldOffsetForInstance(instance, klass, name, "J");
		return memoryManager.readLong(instance, offset);
	}

	public double getDoubleField(ObjectValue instance, InstanceJavaClass klass, String name) {
		long offset = getFieldOffsetForInstance(instance, klass, name, "D");
		return memoryManager.readDouble(instance, offset);
	}

	public int getIntField(ObjectValue instance, InstanceJavaClass klass, String name) {
		long offset = getFieldOffsetForInstance(instance, klass, name, "I");
		return memoryManager.readInt(instance, offset);
	}

	public float getFloatField(ObjectValue instance, InstanceJavaClass klass, String name) {
		long offset = getFieldOffsetForInstance(instance, klass, name, "F");
		return memoryManager.readFloat(instance, offset);
	}

	public char getCharField(ObjectValue instance, InstanceJavaClass klass, String name) {
		long offset = getFieldOffsetForInstance(instance, klass, name, "C");
		return memoryManager.readChar(instance, offset);
	}

	public short getShortField(ObjectValue instance, InstanceJavaClass klass, String name) {
		long offset = getFieldOffsetForInstance(instance, klass, name, "S");
		return memoryManager.readShort(instance, offset);
	}

	public byte getByteField(ObjectValue instance, InstanceJavaClass klass, String name) {
		long offset = getFieldOffsetForInstance(instance, klass, name, "B");
		return memoryManager.readByte(instance, offset);
	}

	public boolean getBooleanField(ObjectValue instance, InstanceJavaClass klass, String name) {
		long offset = getFieldOffsetForInstance(instance, klass, name, "Z");
		return memoryManager.readBoolean(instance, offset);
	}

	public Value getGenericStaticField(InstanceJavaClass klass, String name, String desc) {
		while(klass != null) {
			long offset = klass.getStaticFieldOffset(name, desc);
			if (offset != -1L) return readGenericValue(desc, offset + memoryManager.getStaticOffset(klass), klass.getOop());
			klass = klass.getSuperClass();
		}
		helper.throwException(symbols.java_lang_NoSuchFieldError(), name);
		return null;
	}

	public Value getStaticField(InstanceJavaClass klass, String name, String desc) {
		while(klass != null) {
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

	public long getStaticLongField(InstanceJavaClass klass, String name) {
		while(klass != null) {
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

	public double getStaticDoubleField(InstanceJavaClass klass, String name) {
		while(klass != null) {
			long offset = klass.getStaticFieldOffset(name, "D");
			if (offset != -1L) {
				MemoryManager memoryManager = this.memoryManager;
				return memoryManager.readDouble(klass.getOop(), offset + memoryManager.getStaticOffset(klass));
			}
			klass = klass.getSuperClass();
		}
		helper.throwException(symbols.java_lang_NoSuchFieldError(), name);
		return 0.0D;
	}

	public int getStaticIntField(InstanceJavaClass klass, String name) {
		while(klass != null) {
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

	public float getStaticFloatField(InstanceJavaClass klass, String name) {
		while(klass != null) {
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

	public char getStaticCharField(InstanceJavaClass klass, String name) {
		while(klass != null) {
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

	public short getStaticShortField(InstanceJavaClass klass, String name) {
		while(klass != null) {
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

	public byte getStaticByteField(InstanceJavaClass klass, String name) {
		while(klass != null) {
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

	public boolean getStaticBooleanField(InstanceJavaClass klass, String name) {
		while(klass != null) {
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

	public void putStaticGenericField(InstanceJavaClass klass, String name, String desc, Value value) {
		while(klass != null) {
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

	public void putStaticField(InstanceJavaClass klass, String name, String desc, ObjectValue value) {
		while(klass != null) {
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

	public void putStaticLongField(InstanceJavaClass klass, String name, long value) {
		while(klass != null) {
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

	public void putStaticDoubleField(InstanceJavaClass klass, String name, double value) {
		while(klass != null) {
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

	public void putStaticIntField(InstanceJavaClass klass, String name, int value) {
		while(klass != null) {
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

	public void putStaticFloatField(InstanceJavaClass klass, String name, float value) {
		while(klass != null) {
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

	public void putStaticCharField(InstanceJavaClass klass, String name, char value) {
		while(klass != null) {
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

	public void putStaticShortField(InstanceJavaClass klass, String name, short value) {
		while(klass != null) {
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

	public void putStaticByteField(InstanceJavaClass klass, String name, byte value) {
		while(klass != null) {
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

	public void putStaticBooleanField(InstanceJavaClass klass, String name, boolean value) {
		while(klass != null) {
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

	public Value readGenericValue(String desc, long offset, InstanceValue instance) {
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

	public void writeGenericValue(ObjectValue instance, String desc, Value value, long offset) {
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
