package dev.xdark.ssvm.memory;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.mirror.ArrayJavaClass;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.mirror.JavaClass;
import dev.xdark.ssvm.util.UnsafeUtil;
import dev.xdark.ssvm.value.ClassValue;
import dev.xdark.ssvm.value.*;
import sun.misc.Unsafe;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Simple and dumb implementation of a memory manager.
 *
 * @author xDark
 */
public class SimpleMemoryManager implements MemoryManager {

	private static final long OBJECT_HEADER_SIZE = Unsafe.ADDRESS_SIZE;
	private static final long ARRAY_HEADER_SIZE = OBJECT_HEADER_SIZE + 4;
	private final Set<Memory> memoryBlocks = Collections.newSetFromMap(new WeakHashMap<>());
	private final Map<Memory, Value> objects = new WeakHashMap<>();

	private final VirtualMachine vm;

	/**
	 * @param vm
	 * 		VM instance.
	 */
	public SimpleMemoryManager(VirtualMachine vm) {
		this.vm = vm;
	}

	@Override
	public Memory allocateDirect(long bytes) {
		return newMemoryBlock(bytes, true);
	}

	@Override
	public Memory allocateHeap(long bytes) {
		return newMemoryBlock(bytes, false);
	}

	@Override
	public boolean freeMemory(long address) {
		return memoryBlocks.remove(new Memory(null, null, address, false));
	}

	@Override
	public boolean isValidAddress(long address) {
		return memoryBlocks.contains(new Memory(null, null, address, false));
	}

	@Override
	public InstanceValue newInstance(InstanceJavaClass javaClass) {
		var memory = allocateObjectMemory(javaClass);
		setClass(memory, javaClass);
		var value = new InstanceValue(memory);
		objects.put(memory, value);
		return value;
	}

	@Override
	public ArrayValue newArray(ArrayJavaClass javaClass, int length, long componentSize) {
		var memory = allocateArrayMemory(length, componentSize);
		setClass(memory, javaClass);
		memory.getData().putInt((int) OBJECT_HEADER_SIZE, length);
		return new ArrayValue(memory);
	}

	@Override
	public long readLong(InstanceValue object, long offset) {
		return object.getMemory().getData().getLong((int) (OBJECT_HEADER_SIZE + validate(offset)));
	}

	@Override
	public double readDouble(InstanceValue object, long offset) {
		return object.getMemory().getData().getDouble((int) (OBJECT_HEADER_SIZE + validate(offset)));
	}

	@Override
	public int readInt(InstanceValue object, long offset) {
		return object.getMemory().getData().getInt((int) (OBJECT_HEADER_SIZE + validate(offset)));
	}

	@Override
	public float readFloat(InstanceValue object, long offset) {
		return object.getMemory().getData().getFloat((int) (OBJECT_HEADER_SIZE + validate(offset)));
	}

	@Override
	public char readChar(InstanceValue object, long offset) {
		return object.getMemory().getData().getChar((int) (OBJECT_HEADER_SIZE + validate(offset)));
	}

	@Override
	public short readShort(InstanceValue object, long offset) {
		return object.getMemory().getData().getShort((int) (OBJECT_HEADER_SIZE + validate(offset)));
	}

	@Override
	public byte readByte(InstanceValue object, long offset) {
		return object.getMemory().getData().get((int) (OBJECT_HEADER_SIZE + validate(offset)));
	}

	@Override
	public boolean readBoolean(InstanceValue object, long offset) {
		return readByte(object, offset) != 0;
	}

	@Override
	public Object readOop(InstanceValue object, long offset) {
		return UnsafeUtil.byAddress(object.getMemory().getData().getLong((int) (OBJECT_HEADER_SIZE + validate(offset))));
	}

	@Override
	public Value readValue(InstanceValue object, long offset) {
		var address = object.getMemory().getData().getLong((int) (OBJECT_HEADER_SIZE + validate(offset)));
		return objects.get(new Memory(null, null, address, false));
	}

	@Override
	public JavaClass readClass(ObjectValue object) {
		return (JavaClass) UnsafeUtil.byAddress(object.getMemory().getData().getLong(0));
	}

	@Override
	public int readArrayLength(ArrayValue array) {
		return array.getMemory().getData().getInt((int) OBJECT_HEADER_SIZE);
	}

	@Override
	public void writeLong(InstanceValue object, long offset, long value) {
		object.getMemory().getData().putLong((int) (OBJECT_HEADER_SIZE + validate(offset)), value);
	}

	@Override
	public void writeDouble(InstanceValue object, long offset, double value) {
		object.getMemory().getData().putDouble((int) (OBJECT_HEADER_SIZE + validate(offset)), value);
	}

	@Override
	public void writeInt(InstanceValue object, long offset, int value) {
		object.getMemory().getData().putInt((int) (OBJECT_HEADER_SIZE + validate(offset)), value);
	}

	@Override
	public void writeFloat(InstanceValue object, long offset, float value) {
		object.getMemory().getData().putFloat((int) (OBJECT_HEADER_SIZE + validate(offset)), value);
	}

	@Override
	public void writeChar(InstanceValue object, long offset, char value) {
		object.getMemory().getData().putChar((int) (OBJECT_HEADER_SIZE + validate(offset)), value);
	}

	@Override
	public void writeShort(InstanceValue object, long offset, short value) {
		object.getMemory().getData().putShort((int) (OBJECT_HEADER_SIZE + validate(offset)), value);
	}

	@Override
	public void writeByte(InstanceValue object, long offset, byte value) {
		object.getMemory().getData().put((int) (OBJECT_HEADER_SIZE + validate(offset)), value);
	}

	@Override
	public void writeBoolean(InstanceValue object, long offset, boolean value) {
		writeByte(object, offset, (byte) (value ? 1 : 0));
	}

	@Override
	public void writeOop(InstanceValue object, long offset, Object value) {
		object.getMemory().getData().putLong((int) (OBJECT_HEADER_SIZE + validate(offset)), UnsafeUtil.addressOf(value));
	}

	@Override
	public void writeValue(InstanceValue object, long offset, Value value) {
		writeOop(object, offset, value);
	}

	@Override
	public long readLong(ArrayValue array, long offset) {
		return array.getMemory().getData().getLong((int) (ARRAY_HEADER_SIZE + validate(offset)));
	}

	@Override
	public double readDouble(ArrayValue array, long offset) {
		return array.getMemory().getData().getDouble((int) (ARRAY_HEADER_SIZE + validate(offset)));
	}

	@Override
	public int readInt(ArrayValue array, long offset) {
		return array.getMemory().getData().getInt((int) (ARRAY_HEADER_SIZE + validate(offset)));
	}

	@Override
	public float readFloat(ArrayValue array, long offset) {
		return array.getMemory().getData().getFloat((int) (ARRAY_HEADER_SIZE + validate(offset)));
	}

	@Override
	public char readChar(ArrayValue array, long offset) {
		return array.getMemory().getData().getChar((int) (ARRAY_HEADER_SIZE + validate(offset)));
	}

	@Override
	public short readShort(ArrayValue array, long offset) {
		return array.getMemory().getData().getShort((int) (ARRAY_HEADER_SIZE + validate(offset)));
	}

	@Override
	public byte readByte(ArrayValue array, long offset) {
		return array.getMemory().getData().get((int) (ARRAY_HEADER_SIZE + validate(offset)));
	}

	@Override
	public boolean readBoolean(ArrayValue array, long offset) {
		return readByte(array, offset) != 0;
	}

	@Override
	public Object readOop(ArrayValue array, long offset) {
		return UnsafeUtil.byAddress(array.getMemory().getData().getLong((int) (ARRAY_HEADER_SIZE + validate(offset))));
	}

	@Override
	public Value readValue(ArrayValue array, long offset) {
		var address = array.getMemory().getData().getLong((int) (OBJECT_HEADER_SIZE + validate(offset)));
		return objects.get(new Memory(null, null, address, false));
	}

	@Override
	public void writeLong(ArrayValue array, long offset, long value) {
		array.getMemory().getData().putLong((int) (ARRAY_HEADER_SIZE + validate(offset)), value);
	}

	@Override
	public void writeDouble(ArrayValue array, long offset, double value) {
		array.getMemory().getData().putDouble((int) (ARRAY_HEADER_SIZE + validate(offset)), value);
	}

	@Override
	public void writeInt(ArrayValue array, long offset, int value) {
		array.getMemory().getData().putInt((int) (ARRAY_HEADER_SIZE + validate(offset)), value);
	}

	@Override
	public void writeFloat(ArrayValue array, long offset, float value) {
		array.getMemory().getData().putFloat((int) (ARRAY_HEADER_SIZE + validate(offset)), value);
	}

	@Override
	public void writeChar(ArrayValue array, long offset, char value) {
		array.getMemory().getData().putChar((int) (ARRAY_HEADER_SIZE + validate(offset)), value);
	}

	@Override
	public void writeShort(ArrayValue array, long offset, short value) {
		array.getMemory().getData().putShort((int) (ARRAY_HEADER_SIZE + validate(offset)), value);
	}

	@Override
	public void writeByte(ArrayValue array, long offset, byte value) {
		array.getMemory().getData().put((int) (ARRAY_HEADER_SIZE + validate(offset)), value);
	}

	@Override
	public void writeBoolean(ArrayValue array, long offset, boolean value) {
		writeByte(array, offset, (byte) (value ? 1 : 0));
	}

	@Override
	public void writeOop(ArrayValue array, long offset, Object value) {
		array.getMemory().getData().putLong((int) (ARRAY_HEADER_SIZE + validate(offset)), UnsafeUtil.addressOf(value));
	}

	@Override
	public void writeValue(ArrayValue array, long offset, Value value) {
		writeOop(array, offset, value);
	}

	@Override
	public Value newOopForClass(JavaClass javaClass) {
		Memory memory;
		try {
			var jc = vm.findBootstrapClass("java/lang/Class");
			memory = allocateObjectMemory(jc);
			setClass(memory, jc);
		} catch (Exception ex) {
			throw new IllegalStateException("java/lang/Class is missing");
		}
		return new ClassValue(memory, javaClass);
	}

	private Memory newMemoryBlock(long size, boolean isDirect) {
		if (size > Integer.MAX_VALUE) {
			throw new UnsupportedOperationException();
		}
		var block = new Memory(this, ByteBuffer.allocate((int) size), System.currentTimeMillis(), isDirect);
		memoryBlocks.add(block);
		return block;
	}

	private Memory allocateObjectMemory(JavaClass javaClass) {
		var objectSize = javaClass.getLayout().getSize();
		return allocateHeap(OBJECT_HEADER_SIZE + objectSize);
	}

	private Memory allocateArrayMemory(int length, long componentSize) {
		return allocateHeap(ARRAY_HEADER_SIZE + (long) length * componentSize);
	}

	private void setClass(Memory memory, JavaClass jc) {
		var address = UnsafeUtil.addressOf(jc);
		memory.getData().putLong(0, address);
	}

	private static long validate(long off) {
		if (off < 0L) throw new IllegalStateException();
		return off;
	}
}
