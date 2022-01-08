package dev.xdark.ssvm.memory;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.mirror.JavaClass;
import dev.xdark.ssvm.util.UnsafeUtil;
import dev.xdark.ssvm.value.ClassValue;
import dev.xdark.ssvm.value.InstanceValue;
import dev.xdark.ssvm.value.ObjectValue;
import dev.xdark.ssvm.value.Value;
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

	private static final long CLASS_HEADER_SIZE = Unsafe.ADDRESS_SIZE;
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
	public ObjectValue newObject(InstanceJavaClass javaClass) {
		var memory = allocateObjectMemory(javaClass);
		setClass(memory, javaClass);
		var value = new InstanceValue(memory);
		objects.put(memory, value);
		return value;
	}

	@Override
	public long readLong(ObjectValue object, long offset) {
		return object.getMemory().getData().getLong((int) (CLASS_HEADER_SIZE + validate(offset)));
	}

	@Override
	public double readDouble(ObjectValue object, long offset) {
		return object.getMemory().getData().getDouble((int) (CLASS_HEADER_SIZE + validate(offset)));
	}

	@Override
	public int readInt(ObjectValue object, long offset) {
		return object.getMemory().getData().getInt((int) (CLASS_HEADER_SIZE + validate(offset)));
	}

	@Override
	public float readFloat(ObjectValue object, long offset) {
		return object.getMemory().getData().getFloat((int) (CLASS_HEADER_SIZE + validate(offset)));
	}

	@Override
	public char readChar(ObjectValue object, long offset) {
		return object.getMemory().getData().getChar((int) (CLASS_HEADER_SIZE + validate(offset)));
	}

	@Override
	public short readShort(ObjectValue object, long offset) {
		return object.getMemory().getData().getShort((int) (CLASS_HEADER_SIZE + validate(offset)));
	}

	@Override
	public byte readByte(ObjectValue object, long offset) {
		return object.getMemory().getData().get((int) (CLASS_HEADER_SIZE + validate(offset)));
	}

	@Override
	public boolean readBoolean(ObjectValue object, long offset) {
		return readByte(object, offset) != 0;
	}

	@Override
	public Object readOop(ObjectValue object, long offset) {
		var address = object.getMemory().getData().getLong((int) (CLASS_HEADER_SIZE + validate(offset)));
		return UnsafeUtil.byAddress(address);
	}

	@Override
	public Value readValue(ObjectValue object, long offset) {
		var address = object.getMemory().getData().getLong((int) (CLASS_HEADER_SIZE + validate(offset)));
		return objects.get(new Memory(null, null, address, false));
	}

	@Override
	public JavaClass readClass(ObjectValue object) {
		return (JavaClass) UnsafeUtil.byAddress(object.getMemory().getData().getLong(0));
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
		return allocateHeap(CLASS_HEADER_SIZE + objectSize);
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
