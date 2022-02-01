package dev.xdark.ssvm.memory;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.execution.PanicException;
import dev.xdark.ssvm.mirror.ArrayJavaClass;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.mirror.JavaClass;
import dev.xdark.ssvm.util.UnsafeUtil;
import dev.xdark.ssvm.value.*;
import lombok.val;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Simple and dumb implementation of a memory manager.
 *
 * @author xDark
 */
public class SimpleMemoryManager implements MemoryManager {

	private static final ByteOrder ORDER = ByteOrder.nativeOrder();
	private static final int ADDRESS_SIZE = 8;
	private static final long OBJECT_HEADER_SIZE = ADDRESS_SIZE + 4L;
	private static final long ARRAY_LENGTH = ADDRESS_SIZE;
	private final Map<Long, Memory> memoryBlocks = new HashMap<>();
	private final Map<Memory, ObjectValue> objects = new WeakHashMap<>();

	private final VirtualMachine vm;

	/**
	 * @param vm
	 * 		VM instance.
	 */
	public SimpleMemoryManager(VirtualMachine vm) {
		this.vm = vm;
		objects.put(NullValue.INSTANCE.getMemory(), NullValue.INSTANCE);
	}

	@Override
	public Memory allocateDirect(long bytes) {
		return newMemoryBlock(bytes, true);
	}

	@Override
	public Memory reallocateDirect(long address, long bytes) {
		Memory memory = memoryBlocks.remove(address);
		if (memory == null || !memory.isDirect()) {
			throw new PanicException("Segfault");
		}
		if (bytes == 0L) {
			return new SimpleMemory(this, null, 0L, true);
		}
		val buffer = memory.getData();
		val capacity = buffer.capacity();
		if (bytes < capacity) {
			// can we do that?
			// TODO verify
			throw new PanicException("Segfault");
		}
		val newBuffer = alloc((int) bytes);
		newBuffer.put(buffer);
		newBuffer.position(0);
		memory = new SimpleMemory(this, newBuffer, address, true);
		memoryBlocks.put(address, memory);
		return memory;
	}

	@Override
	public Memory allocateHeap(long bytes) {
		return newMemoryBlock(bytes, false);
	}

	@Override
	public boolean freeMemory(long address) {
		val mem = memoryBlocks.remove(address);
		if (mem != null) {
			objects.remove(mem);
			return true;
		}
		return false;
	}

	@Override
	public Memory getMemory(long address) {
		return memoryBlocks.get(address);
	}

	@Override
	public boolean isValidAddress(long address) {
		return memoryBlocks.containsKey(address);
	}

	@Override
	public Value getValue(long address) {
		return objects.get(new SimpleMemory(null, null, address, true));
	}

	@Override
	public InstanceValue newInstance(InstanceJavaClass javaClass) {
		val memory = allocateObjectMemory(javaClass);
		setClass(memory, javaClass);
		val value = new InstanceValue(memory);
		objects.put(memory, value);
		return value;
	}

	@Override
	public <V> JavaValue<V> newJavaInstance(InstanceJavaClass javaClass, V value) {
		val memory = allocateObjectMemory(javaClass);
		setClass(memory, javaClass);
		val wrapper = new JavaValue<>(memory, value);
		objects.put(memory, wrapper);
		return wrapper;
	}

	@Override
	public JavaValue<InstanceJavaClass> newJavaLangClass(InstanceJavaClass javaClass) {
		val memory = allocateClassMemory(javaClass, javaClass);
		val wrapper = new JavaValue<>(memory, javaClass);
		javaClass.setOop(wrapper);
		setClass(memory, javaClass);
		objects.put(memory, wrapper);
		return wrapper;
	}

	@Override
	public ArrayValue newArray(ArrayJavaClass javaClass, int length) {
		val memory = allocateArrayMemory(length, arrayIndexScale(javaClass.getComponentType()));
		setClass(memory, javaClass);
		memory.getData().putInt((int) ARRAY_LENGTH, length);
		val value = new ArrayValue(memory);
		objects.put(memory, value);
		return value;
	}

	@Override
	public long readLong(ObjectValue object, long offset) {
		return object.getMemory().getData().getLong((int) (validate(offset)));
	}

	@Override
	public double readDouble(ObjectValue object, long offset) {
		return object.getMemory().getData().getDouble((int) (validate(offset)));
	}

	@Override
	public int readInt(ObjectValue object, long offset) {
		return object.getMemory().getData().getInt((int) (validate(offset)));
	}

	@Override
	public float readFloat(ObjectValue object, long offset) {
		return object.getMemory().getData().getFloat((int) (validate(offset)));
	}

	@Override
	public char readChar(ObjectValue object, long offset) {
		return object.getMemory().getData().getChar((int) (validate(offset)));
	}

	@Override
	public short readShort(ObjectValue object, long offset) {
		return object.getMemory().getData().getShort((int) (validate(offset)));
	}

	@Override
	public byte readByte(ObjectValue object, long offset) {
		return object.getMemory().getData().get((int) (validate(offset)));
	}

	@Override
	public boolean readBoolean(ObjectValue object, long offset) {
		return readByte(object, offset) != 0;
	}

	@Override
	public Object readOop(ObjectValue object, long offset) {
		return UnsafeUtil.byAddress(object.getMemory().getData().getLong((int) (validate(offset))));
	}

	@Override
	public ObjectValue readValue(ObjectValue object, long offset) {
		val address = object.getMemory().getData().getLong((int) (validate(offset)));
		return objects.get(new SimpleMemory(null, null, address, false));
	}

	@Override
	public JavaClass readClass(ObjectValue object) {
		val value = objects.get(new SimpleMemory(null, null, object.getMemory().getData().getLong(0), false));
		if (!(value instanceof JavaValue)) {
			throw new PanicException("Segfault");
		}
		val wrapper = ((JavaValue<?>) value).getValue();
		if (!(wrapper instanceof JavaClass)) {
			throw new PanicException("Segfault");
		}
		return (JavaClass) wrapper;
	}

	@Override
	public int readArrayLength(ArrayValue array) {
		return array.getMemory().getData().getInt((int) ARRAY_LENGTH);
	}

	@Override
	public void writeLong(ObjectValue object, long offset, long value) {
		object.getMemory().getData().putLong((int) (validate(offset)), value);
	}

	@Override
	public void writeDouble(ObjectValue object, long offset, double value) {
		object.getMemory().getData().putDouble((int) (validate(offset)), value);
	}

	@Override
	public void writeInt(ObjectValue object, long offset, int value) {
		object.getMemory().getData().putInt((int) (validate(offset)), value);
	}

	@Override
	public void writeFloat(ObjectValue object, long offset, float value) {
		object.getMemory().getData().putFloat((int) (validate(offset)), value);
	}

	@Override
	public void writeChar(ObjectValue object, long offset, char value) {
		object.getMemory().getData().putChar((int) (validate(offset)), value);
	}

	@Override
	public void writeShort(ObjectValue object, long offset, short value) {
		object.getMemory().getData().putShort((int) (validate(offset)), value);
	}

	@Override
	public void writeByte(ObjectValue object, long offset, byte value) {
		object.getMemory().getData().put((int) (validate(offset)), value);
	}

	@Override
	public void writeBoolean(ObjectValue object, long offset, boolean value) {
		writeByte(object, offset, (byte) (value ? 1 : 0));
	}

	@Override
	public void writeOop(ObjectValue object, long offset, Object value) {
		object.getMemory().getData().putLong((int) (validate(offset)), UnsafeUtil.addressOf(value));
	}

	@Override
	public void writeValue(ObjectValue object, long offset, ObjectValue value) {
		object.getMemory().getData().putLong((int) (validate(offset)), value.getMemory().getAddress());
	}

	@Override
	public <C extends JavaClass> JavaValue<C> setOopForClass(C javaClass) {
		val jlc = vm.findBootstrapClass("java/lang/Class");
		val memory = allocateClassMemory(jlc, javaClass);
		setClass(memory, jlc);
		val wrapper = new JavaValue<>(memory, javaClass);
		objects.put(memory, wrapper);
		return wrapper;
	}

	@Override
	public ByteOrder getByteOrder() {
		return ORDER;
	}

	@Override
	public int addressSize() {
		return ADDRESS_SIZE;
	}

	@Override
	public int pageSize() {
		return UnsafeUtil.get().pageSize();
	}

	@Override
	public int valueBaseOffset(ObjectValue value) {
		return (int) OBJECT_HEADER_SIZE;
	}

	@Override
	public int valueBaseOffset(JavaClass value) {
		return (int) OBJECT_HEADER_SIZE;
	}

	@Override
	public int arrayBaseOffset(JavaClass javaClass) {
		return (int) OBJECT_HEADER_SIZE;
	}

	@Override
	public int arrayBaseOffset(ArrayValue array) {
		return (int) OBJECT_HEADER_SIZE;
	}

	@Override
	public int arrayBaseOffset(Class<?> javaClass) {
		return (int) OBJECT_HEADER_SIZE;
	}

	@Override
	public int arrayIndexScale(JavaClass javaClass) {
		val primitives = vm.getPrimitives();
		if (javaClass == primitives.longPrimitive || javaClass == primitives.doublePrimitive) return 8;
		if (javaClass == primitives.intPrimitive || javaClass == primitives.floatPrimitive) return 4;
		if (javaClass == primitives.charPrimitive || javaClass == primitives.shortPrimitive) return 2;
		if (javaClass == primitives.bytePrimitive || javaClass == primitives.booleanPrimitive) return 1;
		return 8;
	}

	@Override
	public int arrayIndexScale(Class<?> javaClass) {
		if (javaClass == long.class || javaClass == double.class) return 8;
		if (javaClass == int.class || javaClass == float.class) return 4;
		if (javaClass == char.class || javaClass == short.class) return 2;
		if (javaClass == byte.class || javaClass == boolean.class) return 1;
		return 8;
	}

	@Override
	public Memory zero() {
		return NullValue.INSTANCE.getMemory();
	}

	@Override
	public long getStaticOffset(JavaClass jc) {
		val jlc = vm.findBootstrapClass("java/lang/Class");
		return OBJECT_HEADER_SIZE + jlc.getVirtualFieldLayout().getSize();
	}

	private Memory newMemoryBlock(long size, boolean isDirect) {
		if (size > Integer.MAX_VALUE) {
			vm.getHelper().throwException(vm.getSymbols().java_lang_OutOfMemoryError);
			return null;
		}
		val rng = ThreadLocalRandom.current();
		long address;
		do {
			address = rng.nextLong() & 0xFFFFFFFFL;
		} while (memoryBlocks.containsKey(address));
		val block = new SimpleMemory(this, alloc((int) size), address, isDirect);
		memoryBlocks.put(address, block);
		return block;
	}

	private Memory allocateObjectMemory(JavaClass javaClass) {
		val objectSize = OBJECT_HEADER_SIZE + javaClass.getVirtualFieldLayout().getSize();
		return allocateHeap(objectSize);
	}

	private Memory allocateClassMemory(JavaClass jlc, JavaClass javaClass) {
		val size = OBJECT_HEADER_SIZE + jlc.getVirtualFieldLayout().getSize() + javaClass.getStaticFieldLayout().getSize();
		return allocateHeap(size);
	}

	private Memory allocateArrayMemory(int length, long componentSize) {
		return allocateHeap(OBJECT_HEADER_SIZE + (long) length * componentSize);
	}

	private void setClass(Memory memory, JavaClass jc) {
		val address = jc.getOop().getMemory().getAddress();
		memory.getData().putLong(0, address);
	}

	private static long validate(long off) {
		if (off < 0L) throw new PanicException("Segfault");
		return off;
	}

	private static ByteBuffer alloc(int size) {
		return ByteBuffer.allocate(size).order(ORDER);
	}
}
