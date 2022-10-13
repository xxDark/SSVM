package dev.xdark.ssvm.operation;

import dev.xdark.ssvm.LinkResolver;
import dev.xdark.ssvm.memory.management.MemoryManager;
import dev.xdark.ssvm.mirror.member.JavaField;
import dev.xdark.ssvm.mirror.type.InstanceClass;
import dev.xdark.ssvm.value.InstanceValue;
import dev.xdark.ssvm.value.ObjectValue;
import lombok.RequiredArgsConstructor;

/**
 * Default implementation.
 *
 * @author xDark
 */
@RequiredArgsConstructor
public final class DefaultFieldOperations implements FieldOperations {

	private final MemoryManager memoryManager;
	private final LinkResolver linkResolver;
	private final VerificationOperations verificationOperations;

	@Override
	public void putReference(ObjectValue instance, InstanceClass klass, String name, String desc, ObjectValue value) {
		long offset = getFieldOffsetForInstance(instance, klass, name, desc);
		memoryManager.writeValue(instance, offset, value);
	}

	@Override
	public void putReference(ObjectValue instance, String name, String desc, ObjectValue value) {
		putReference(instance, verificationOperations.<InstanceValue>checkNotNull(instance).getJavaClass(), name, desc, value);
	}

	@Override
	public void putLong(ObjectValue instance, InstanceClass klass, String name, long value) {
		long offset = getFieldOffsetForInstance(instance, klass, name, "J");
		instance.getData().writeLong(offset, value);
	}

	@Override
	public void putLong(ObjectValue instance, String name, long value) {
		putLong(instance, verificationOperations.<InstanceValue>checkNotNull(instance).getJavaClass(), name, value);
	}

	@Override
	public void putDouble(ObjectValue instance, InstanceClass klass, String name, double value) {
		long offset = getFieldOffsetForInstance(instance, klass, name, "D");
		instance.getData().writeLong(offset, Double.doubleToRawLongBits(value));
	}

	@Override
	public void putDouble(ObjectValue instance, String name, double value) {
		putDouble(instance, verificationOperations.<InstanceValue>checkNotNull(instance).getJavaClass(), name, value);
	}

	@Override
	public void putInt(ObjectValue instance, InstanceClass klass, String name, int value) {
		long offset = getFieldOffsetForInstance(instance, klass, name, "I");
		instance.getData().writeInt(offset, value);
	}

	@Override
	public void putInt(ObjectValue instance, String name, int value) {
		putInt(instance, verificationOperations.<InstanceValue>checkNotNull(instance).getJavaClass(), name, value);
	}

	@Override
	public void putFloat(ObjectValue instance, InstanceClass klass, String name, float value) {
		long offset = getFieldOffsetForInstance(instance, klass, name, "F");
		instance.getData().writeInt(offset, Float.floatToRawIntBits(value));
	}

	@Override
	public void putFloat(ObjectValue instance, String name, float value) {
		putFloat(instance, verificationOperations.<InstanceValue>checkNotNull(instance).getJavaClass(), name, value);
	}

	@Override
	public void putChar(ObjectValue instance, InstanceClass klass, String name, char value) {
		long offset = getFieldOffsetForInstance(instance, klass, name, "C");
		instance.getData().writeChar(offset, value);
	}

	@Override
	public void putChar(ObjectValue instance, String name, char value) {
		putChar(instance, verificationOperations.<InstanceValue>checkNotNull(instance).getJavaClass(), name, value);
	}

	@Override
	public void putShort(ObjectValue instance, InstanceClass klass, String name, short value) {
		long offset = getFieldOffsetForInstance(instance, klass, name, "S");
		instance.getData().writeShort(offset, value);
	}

	@Override
	public void putShort(ObjectValue instance, String name, short value) {
		putShort(instance, verificationOperations.<InstanceValue>checkNotNull(instance).getJavaClass(), name, value);
	}

	@Override
	public void putByte(ObjectValue instance, InstanceClass klass, String name, byte value) {
		long offset = getFieldOffsetForInstance(instance, klass, name, "B");
		instance.getData().writeByte(offset, value);
	}

	@Override
	public void putByte(ObjectValue instance, String name, byte value) {
		putByte(instance, verificationOperations.<InstanceValue>checkNotNull(instance).getJavaClass(), name, value);
	}

	@Override
	public void putBoolean(ObjectValue instance, InstanceClass klass, String name, boolean value) {
		long offset = getFieldOffsetForInstance(instance, klass, name, "Z");
		instance.getData().writeByte(offset, (byte) (value ? 1 : 0));
	}

	@Override
	public void putBoolean(ObjectValue instance, String name, boolean value) {
		putBoolean(instance, verificationOperations.<InstanceValue>checkNotNull(instance).getJavaClass(), name, value);
	}

	@Override
	public ObjectValue getReference(ObjectValue instance, InstanceClass klass, String name, String desc) {
		long offset = getFieldOffsetForInstance(instance, klass, name, desc);
		return memoryManager.readReference(instance, offset);
	}

	@Override
	public ObjectValue getReference(ObjectValue instance, String name, String desc) {
		return getReference(instance, verificationOperations.<InstanceValue>checkNotNull(instance).getJavaClass(), name, desc);
	}

	@Override
	public long getLong(ObjectValue instance, InstanceClass klass, String name) {
		long offset = getFieldOffsetForInstance(instance, klass, name, "J");
		return instance.getData().readLong(offset);
	}

	@Override
	public long getLong(ObjectValue instance, String name) {
		return getLong(instance, verificationOperations.<InstanceValue>checkNotNull(instance).getJavaClass(), name);
	}

	@Override
	public double getDouble(ObjectValue instance, InstanceClass klass, String name) {
		long offset = getFieldOffsetForInstance(instance, klass, name, "D");
		return Double.longBitsToDouble(instance.getData().readLong(offset));
	}

	@Override
	public double getDouble(ObjectValue instance, String name) {
		return getDouble(instance, verificationOperations.<InstanceValue>checkNotNull(instance).getJavaClass(), name);
	}

	@Override
	public int getInt(ObjectValue instance, InstanceClass klass, String name) {
		long offset = getFieldOffsetForInstance(instance, klass, name, "I");
		return instance.getData().readInt(offset);
	}

	@Override
	public int getInt(ObjectValue instance, String name) {
		return getInt(instance, verificationOperations.<InstanceValue>checkNotNull(instance).getJavaClass(), name);
	}

	@Override
	public float getFloat(ObjectValue instance, InstanceClass klass, String name) {
		long offset = getFieldOffsetForInstance(instance, klass, name, "F");
		return Float.intBitsToFloat(instance.getData().readInt(offset));
	}

	@Override
	public float getFloat(ObjectValue instance, String name) {
		return getFloat(instance, verificationOperations.<InstanceValue>checkNotNull(instance).getJavaClass(), name);
	}

	@Override
	public char getChar(ObjectValue instance, InstanceClass klass, String name) {
		long offset = getFieldOffsetForInstance(instance, klass, name, "C");
		return instance.getData().readChar(offset);
	}

	@Override
	public char getChar(ObjectValue instance, String name) {
		return getChar(instance, verificationOperations.<InstanceValue>checkNotNull(instance).getJavaClass(), name);
	}

	@Override
	public short getShort(ObjectValue instance, InstanceClass klass, String name) {
		long offset = getFieldOffsetForInstance(instance, klass, name, "S");
		return instance.getData().readShort(offset);
	}

	@Override
	public short getShort(ObjectValue instance, String name) {
		return getShort(instance, verificationOperations.<InstanceValue>checkNotNull(instance).getJavaClass(), name);
	}

	@Override
	public byte getByte(ObjectValue instance, InstanceClass klass, String name) {
		long offset = getFieldOffsetForInstance(instance, klass, name, "B");
		return instance.getData().readByte(offset);
	}

	@Override
	public byte getByte(ObjectValue instance, String name) {
		return getByte(instance, verificationOperations.<InstanceValue>checkNotNull(instance).getJavaClass(), name);
	}

	@Override
	public boolean getBoolean(ObjectValue instance, InstanceClass klass, String name) {
		long offset = getFieldOffsetForInstance(instance, klass, name, "Z");
		return instance.getData().readByte(offset) != 0;
	}

	@Override
	public boolean getBoolean(ObjectValue instance, String name) {
		return getBoolean(instance, verificationOperations.<InstanceValue>checkNotNull(instance).getJavaClass(), name);
	}

	@Override
	public ObjectValue getReference(InstanceClass klass, String name, String desc) {
		JavaField field = linkResolver.resolveStaticField(klass, name, desc);
		klass = field.getOwner();
		return memoryManager.readReference(klass.getOop(), field.getOffset());
	}

	@Override
	public long getLong(InstanceClass klass, String name) {
		JavaField field = linkResolver.resolveStaticField(klass, name, "J");
		klass = field.getOwner();
		return klass.getOop().getData().readLong(field.getOffset());
	}

	@Override
	public double getDouble(InstanceClass klass, String name) {
		JavaField field = linkResolver.resolveStaticField(klass, name, "D");
		klass = field.getOwner();
		return Double.longBitsToDouble(klass.getOop().getData().readLong(field.getOffset()));
	}

	@Override
	public int getInt(InstanceClass klass, String name) {
		JavaField field = linkResolver.resolveStaticField(klass, name, "I");
		klass = field.getOwner();
		return klass.getOop().getData().readInt(field.getOffset());
	}

	@Override
	public float getFloat(InstanceClass klass, String name) {
		JavaField field = linkResolver.resolveStaticField(klass, name, "F");
		klass = field.getOwner();
		return Float.intBitsToFloat(klass.getOop().getData().readInt(field.getOffset()));
	}

	@Override
	public char getChar(InstanceClass klass, String name) {
		JavaField field = linkResolver.resolveStaticField(klass, name, "C");
		klass = field.getOwner();
		return klass.getOop().getData().readChar(field.getOffset());
	}

	@Override
	public short getShort(InstanceClass klass, String name) {
		JavaField field = linkResolver.resolveStaticField(klass, name, "S");
		klass = field.getOwner();
		return klass.getOop().getData().readShort(field.getOffset());
	}

	@Override
	public byte getByte(InstanceClass klass, String name) {
		JavaField field = linkResolver.resolveStaticField(klass, name, "B");
		klass = field.getOwner();
		return klass.getOop().getData().readByte(field.getOffset());
	}

	@Override
	public boolean getBoolean(InstanceClass klass, String name) {
		JavaField field = linkResolver.resolveStaticField(klass, name, "Z");
		klass = field.getOwner();
		return klass.getOop().getData().readByte(field.getOffset()) != 0;
	}
	//</editor-fold>

	//<editor-fold desc="Static field put methods">

	@Override
	public void putReference(InstanceClass klass, String name, String desc, ObjectValue value) {
		JavaField field = linkResolver.resolveStaticField(klass, name, desc);
		klass = field.getOwner();
		MemoryManager memoryManager = this.memoryManager;
		memoryManager.writeValue(klass.getOop(), field.getOffset(), value);
	}

	@Override
	public void putLong(InstanceClass klass, String name, long value) {
		JavaField field = linkResolver.resolveStaticField(klass, name, "J");
		klass = field.getOwner();
		MemoryManager memoryManager = this.memoryManager;
		klass.getOop().getData().writeLong(field.getOffset(), value);
	}

	@Override
	public void putDouble(InstanceClass klass, String name, double value) {
		JavaField field = linkResolver.resolveStaticField(klass, name, "D");
		klass = field.getOwner();
		klass.getOop().getData().writeLong(field.getOffset(), Double.doubleToRawLongBits(value));
	}

	@Override
	public void putInt(InstanceClass klass, String name, int value) {
		JavaField field = linkResolver.resolveStaticField(klass, name, "I");
		klass = field.getOwner();
		klass.getOop().getData().writeInt(field.getOffset(), value);
	}

	@Override
	public void putFloat(InstanceClass klass, String name, float value) {
		JavaField field = linkResolver.resolveStaticField(klass, name, "F");
		klass = field.getOwner();
		klass.getOop().getData().writeInt(field.getOffset(), Float.floatToRawIntBits(value));
	}

	@Override
	public void putChar(InstanceClass klass, String name, char value) {
		JavaField field = linkResolver.resolveStaticField(klass, name, "C");
		klass = field.getOwner();
		klass.getOop().getData().writeChar(field.getOffset(), value);
	}

	@Override
	public void putShort(InstanceClass klass, String name, short value) {
		JavaField field = linkResolver.resolveStaticField(klass, name, "S");
		klass = field.getOwner();
		klass.getOop().getData().writeShort(field.getOffset(), value);
	}

	@Override
	public void putByte(InstanceClass klass, String name, byte value) {
		JavaField field = linkResolver.resolveStaticField(klass, name, "B");
		klass = field.getOwner();
		klass.getOop().getData().writeByte(field.getOffset(), value);
	}

	@Override
	public void putBoolean(InstanceClass klass, String name, boolean value) {
		JavaField field = linkResolver.resolveStaticField(klass, name, "Z");
		klass = field.getOwner();
		klass.getOop().getData().writeByte(field.getOffset(), (byte) (value ? 1 : 0));
	}

	private long getFieldOffsetForInstance(ObjectValue instance, InstanceClass klass, String name, String desc) {
		verificationOperations.checkNotNull(instance);
		JavaField field = linkResolver.resolveVirtualField(klass, name, desc);
		return field.getOffset();
	}
}
